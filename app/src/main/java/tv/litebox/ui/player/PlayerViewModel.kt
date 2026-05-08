package tv.litebox.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tv.litebox.LiteBoxApp

data class PlayerUiState(
    val isLoading: Boolean = true,
    val player: Player? = null,
    val error: String? = null,
)

class PlayerViewModel(
    app: Application,
    private val mediaId: String,
) : AndroidViewModel(app) {

    private val db = LiteBoxApp.instance.database
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var player: ExoPlayer? = null

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val entity = db.mediaItemDao().getById(mediaId)
            if (entity == null) {
                _uiState.value = PlayerUiState(isLoading = false, error = "Media not found")
                return@launch
            }
            val item = entity.toDomain()
            val uri = item.uri

            // URI routing: reject unsupported schemes before touching ExoPlayer
            when {
                uri.startsWith("smb://") || uri.startsWith("nfs://") -> {
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        error = "SMB/NFS not yet supported — use a plugin source",
                    )
                    return@launch
                }
                uri.startsWith("plugin://") -> {
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        error = "Plugin playback not yet implemented",
                    )
                    return@launch
                }
                // file://, http://, https://, rtmp://, rtsp:// all pass through
            }

            val exo = ExoPlayer.Builder(getApplication())
                .build()
                .apply {
                    setMediaItem(ExoMediaItem.fromUri(uri))
                    prepare()
                    playWhenReady = true
                    if (item.resumePosition > 0) seekTo(item.resumePosition)
                }

            // Player.Listener: track isPlaying, detect STATE_ENDED and errors
            exo.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    _isPlaying.value = isPlayingNow
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        viewModelScope.launch {
                            db.mediaItemDao().markWatched(mediaId)
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Playback error",
                    )
                }
            })

            player = exo
            _uiState.value = PlayerUiState(isLoading = false, player = exo)

            // Auto-save resume position every 10 seconds while playing
            launch {
                while (isActive) {
                    delay(10_000L)
                    val p = player ?: break
                    if (p.isPlaying) {
                        db.mediaItemDao().updateResumePosition(mediaId, p.currentPosition)
                    }
                }
            }
        }
    }

    /** Seek forward 10 seconds. */
    fun seekForward() {
        player?.let { it.seekTo(it.currentPosition + 10_000L) }
    }

    /** Seek back 10 seconds (clamped to 0). */
    fun seekBack() {
        player?.let { it.seekTo((it.currentPosition - 10_000L).coerceAtLeast(0L)) }
    }

    /** Toggle between play and pause. */
    fun togglePlayPause() {
        val p = player ?: return
        if (p.isPlaying) p.pause() else p.play()
    }

    fun onPause() {
        val pos = player?.currentPosition ?: 0L
        viewModelScope.launch {
            db.mediaItemDao().updateResumePosition(mediaId, pos)
            if ((player?.duration ?: 0) > 0) {
                val pct = pos.toFloat() / (player?.duration ?: 1)
                if (pct > 0.9f) {
                    db.mediaItemDao().markWatched(mediaId)
                }
            }
        }
        player?.pause()
    }

    override fun onCleared() {
        player?.release()
        player = null
        super.onCleared()
    }

    class Factory(private val mediaId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlayerViewModel(LiteBoxApp.instance, mediaId) as T
    }
}

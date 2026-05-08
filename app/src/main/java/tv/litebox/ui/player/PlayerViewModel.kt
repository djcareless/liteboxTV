package tv.litebox.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

            val exo = ExoPlayer.Builder(getApplication())
                .build()
                .apply {
                    setMediaItem(ExoMediaItem.fromUri(item.uri))
                    prepare()
                    playWhenReady = true
                    if (item.resumePosition > 0) seekTo(item.resumePosition)
                }

            player = exo
            _uiState.value = PlayerUiState(isLoading = false, player = exo)

            // Auto-save resume position every 10s
            // (full implementation uses a coroutine timer + position listener)
        }
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

package tv.litebox.ui.player

import android.view.KeyEvent as AndroidKeyEvent
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaId: String,
    onBack: () -> Unit,
    vm: PlayerViewModel = viewModel(
        factory = PlayerViewModel.Factory(mediaId)
    ),
) {
    @Suppress("UNUSED_VARIABLE")
    val context = LocalContext.current
    val uiState by vm.uiState.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    BackHandler { onBack() }

    DisposableEffect(Unit) {
        onDispose { vm.onPause() }
    }

    // ── Overlay visibility ──────────────────────────────────────────────────
    var showOverlay by rememberSaveable { mutableStateOf(false) }
    // Incrementing this key re-starts the auto-hide LaunchedEffect
    var overlayTrigger by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(overlayTrigger) {
        if (overlayTrigger > 0) {
            delay(3_000L)
            showOverlay = false
        }
    }

    fun showControls() {
        showOverlay = true
        overlayTrigger++
    }

    // ── Position polling ────────────────────────────────────────────────────
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    LaunchedEffect(uiState.player) {
        while (uiState.player != null) {
            delay(500L)
            currentPosition = uiState.player!!.currentPosition
            totalDuration = uiState.player!!.duration.coerceAtLeast(0L)
        }
    }

    // ── Focus (needed to receive key events on TV) ──────────────────────────
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(uiState.player) {
        if (uiState.player != null) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    // ── Root container ──────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (uiState.player != null &&
                    keyEvent.nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN
                ) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
                            vm.seekBack(); showControls(); true
                        }
                        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                            vm.seekForward(); showControls(); true
                        }
                        AndroidKeyEvent.KEYCODE_DPAD_CENTER,
                        AndroidKeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            vm.togglePlayPause(); showControls(); true
                        }
                        else -> { showControls(); false }
                    }
                } else {
                    false
                }
            }
            .clickable { if (uiState.player != null) showControls() },
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()

            uiState.error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Playback error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            uiState.player != null -> {
                // ── Video rendering (PlayerView, no default controller) ──────
                AndroidView(
                    factory = {
                        PlayerView(it).apply {
                            player = uiState.player
                            useController = false   // custom overlay handles controls
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                // ── Custom controls overlay ──────────────────────────────────
                AnimatedVisibility(
                    visible = showOverlay,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                    ) {
                        // Center row: ◀◀ ▶/⏸ ▶▶
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Seek back -10 s
                            IconButton(onClick = { vm.seekBack(); showControls() }) {
                                Icon(
                                    imageVector = Icons.Filled.FastRewind,
                                    contentDescription = "Seek back 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp),
                                )
                            }

                            // Play / Pause
                            IconButton(onClick = { vm.togglePlayPause(); showControls() }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause
                                                  else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp),
                                )
                            }

                            // Seek forward +10 s
                            IconButton(onClick = { vm.seekForward(); showControls() }) {
                                Icon(
                                    imageVector = Icons.Filled.FastForward,
                                    contentDescription = "Seek forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp),
                                )
                            }
                        }

                        // Position / duration at bottom
                        Text(
                            text = "${formatMs(currentPosition)} / ${formatMs(totalDuration)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Format milliseconds as [H:]MM:SS. */
private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000L
    val hours = totalSec / 3600L
    val minutes = (totalSec % 3600L) / 60L
    val seconds = totalSec % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

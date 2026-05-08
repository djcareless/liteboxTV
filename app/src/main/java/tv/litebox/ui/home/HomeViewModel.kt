package tv.litebox.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tv.litebox.LiteBoxApp
import tv.litebox.domain.model.MediaItem
import tv.litebox.domain.model.MediaType

data class HomeUiState(
    val continueWatching: List<MediaItem> = emptyList(),
    val recentMovies: List<MediaItem> = emptyList(),
    val recentTvShows: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel : ViewModel() {

    private val db = LiteBoxApp.instance.database
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                db.mediaItemDao().observeContinueWatching(),
                db.mediaItemDao().observeByType(MediaType.MOVIE.name),
                db.mediaItemDao().observeByType(MediaType.TV_SHOW.name),
            ) { continueWatching, movies, tvShows ->
                HomeUiState(
                    continueWatching = continueWatching.map { it.toDomain() },
                    recentMovies = movies.take(20).map { it.toDomain() },
                    recentTvShows = tvShows.take(20).map { it.toDomain() },
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

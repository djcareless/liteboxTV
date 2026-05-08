package tv.litebox.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tv.litebox.LiteBoxApp
import tv.litebox.domain.model.MediaItem
import tv.litebox.domain.model.MediaType

class LibraryViewModel(sourceType: String) : ViewModel() {

    private val db = LiteBoxApp.instance.database

    val items = db.mediaItemDao()
        .observeByType(sourceType)
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList<MediaItem>())

    class Factory(private val sourceType: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LibraryViewModel(sourceType) as T
    }
}

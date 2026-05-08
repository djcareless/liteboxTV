package tv.litebox.ui.plugins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.litebox.LiteBoxApp
import tv.litebox.domain.model.InstalledPlugin

data class RegistryEntry(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    val manifestUrl: String,
    val author: String,
)

sealed class InstallState {
    data object Idle : InstallState()
    data object Loading : InstallState()
    data class Success(val plugin: InstalledPlugin) : InstallState()
    data class Error(val message: String) : InstallState()
}

class PluginsViewModel : ViewModel() {

    private val pluginManager = LiteBoxApp.instance.pluginManager

    val plugins: StateFlow<List<InstalledPlugin>> = pluginManager.plugins
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val registryPlugins = listOf(
        RegistryEntry(
            id = "tv.litebox.plugin.tmdb",
            name = "TMDB Scraper",
            description = "Fetch movie and TV metadata, artwork, and ratings from The Movie Database.",
            type = "SCRAPER",
            manifestUrl = "https://plugins.litebox.tv/tmdb/plugin.json",
            author = "LiteBox",
        ),
        RegistryEntry(
            id = "tv.litebox.plugin.tvdb",
            name = "TVDB Scraper",
            description = "TV show metadata and episode info from The TV Database.",
            type = "SCRAPER",
            manifestUrl = "https://plugins.litebox.tv/tvdb/plugin.json",
            author = "LiteBox",
        ),
        RegistryEntry(
            id = "tv.litebox.plugin.iptv",
            name = "IPTV Source",
            description = "Add IPTV playlists (M3U) and EPG guide data as a media source.",
            type = "SOURCE",
            manifestUrl = "https://plugins.litebox.tv/iptv/plugin.json",
            author = "LiteBox",
        ),
    )

    private val _installState = MutableStateFlow<InstallState>(InstallState.Idle)
    val installState: StateFlow<InstallState> = _installState.asStateFlow()

    fun installPlugin(url: String) {
        val cleanedUrl = url.trim()
        if (cleanedUrl.isBlank()) {
            _installState.value = InstallState.Error("Enter a plugin manifest URL")
            clearTransientState()
            return
        }

        viewModelScope.launch {
            _installState.value = InstallState.Loading
            pluginManager.installFromUrl(cleanedUrl)
                .onSuccess { plugin ->
                    _installState.value = InstallState.Success(plugin)
                    clearTransientState()
                }
                .onFailure { e ->
                    _installState.value = InstallState.Error(e.message ?: "Unknown error")
                    clearTransientState()
                }
        }
    }

    fun installFromRegistry(entry: RegistryEntry) = installPlugin(entry.manifestUrl)

    fun togglePlugin(pluginId: String, enabled: Boolean) {
        viewModelScope.launch { pluginManager.setEnabled(pluginId, enabled) }
    }

    fun removePlugin(pluginId: String) {
        viewModelScope.launch { pluginManager.uninstall(pluginId) }
    }

    private fun clearTransientState() {
        viewModelScope.launch {
            delay(2_000)
            _installState.value = InstallState.Idle
        }
    }
}

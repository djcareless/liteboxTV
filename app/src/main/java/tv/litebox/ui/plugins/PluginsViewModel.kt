package tv.litebox.ui.plugins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tv.litebox.LiteBoxApp
import tv.litebox.domain.model.InstalledPlugin

sealed class InstallState {
    object Idle : InstallState()
    object Loading : InstallState()
    data class Success(val plugin: InstalledPlugin) : InstallState()
    data class Error(val message: String) : InstallState()
}

class PluginsViewModel : ViewModel() {

    private val pluginManager = LiteBoxApp.instance.pluginManager

    val plugins: StateFlow<List<InstalledPlugin>> = pluginManager.plugins
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _installState = MutableStateFlow<InstallState>(InstallState.Idle)
    val installState: StateFlow<InstallState> = _installState.asStateFlow()

    fun installPlugin(url: String) {
        viewModelScope.launch {
            _installState.value = InstallState.Loading
            pluginManager.installFromUrl(url)
                .onSuccess { plugin -> _installState.value = InstallState.Success(plugin) }
                .onFailure { e -> _installState.value = InstallState.Error(e.message ?: "Unknown error") }
        }
    }

    fun togglePlugin(pluginId: String, enabled: Boolean) {
        viewModelScope.launch { pluginManager.setEnabled(pluginId, enabled) }
    }

    fun removePlugin(pluginId: String) {
        viewModelScope.launch { pluginManager.uninstall(pluginId) }
    }
}

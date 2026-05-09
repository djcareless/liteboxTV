package tv.litebox.ui.plugins

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.litebox.LiteBoxApp
import tv.litebox.domain.model.InstalledPlugin

data class PluginDetailUiState(
    val plugin: InstalledPlugin? = null,
    val editingSettings: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val showUninstallConfirm: Boolean = false,
)

class PluginDetailViewModel(
    private val pluginId: String,
) : ViewModel() {

    private val pluginManager = LiteBoxApp.instance.pluginManager

    private val _uiState = MutableStateFlow(PluginDetailUiState())
    val uiState: StateFlow<PluginDetailUiState> = _uiState.asStateFlow()

    init {
        // Observe the live plugin list and extract the one we care about
        pluginManager.plugins
            .map { plugins -> plugins.find { it.manifest.id == pluginId } }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { plugin ->
                        if (plugin != null) {
                            val current = _uiState.value
                            _uiState.value = current.copy(
                                plugin = plugin,
                                editingSettings = current.editingSettings.takeIf {
                                    it.isNotEmpty()
                                } ?: plugin.settings,
                            )
                        }
                    }
                }
            }
    }

    fun updateSetting(key: String, value: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            editingSettings = current.editingSettings.toMutableMap().apply { put(key, value) },
            saveSuccess = false,
            error = null,
        )
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                pluginManager.updateSettings(pluginId, _uiState.value.editingSettings)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                delay(2_000)
                _uiState.value = _uiState.value.copy(saveSuccess = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save settings",
                )
            }
        }
    }

    fun toggleEnabled() {
        val plugin = _uiState.value.plugin ?: return
        viewModelScope.launch {
            pluginManager.setEnabled(pluginId, !plugin.enabled)
        }
    }

    fun showUninstallConfirm() {
        _uiState.value = _uiState.value.copy(showUninstallConfirm = true)
    }

    fun dismissUninstallConfirm() {
        _uiState.value = _uiState.value.copy(showUninstallConfirm = false)
    }

    fun uninstall() {
        viewModelScope.launch {
            pluginManager.uninstall(pluginId)
        }
    }
}

class PluginDetailViewModelFactory(
    private val pluginId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PluginDetailViewModel(pluginId) as T
    }
}

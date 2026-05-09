package tv.litebox.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import tv.litebox.domain.model.ThemeManifest
import tv.litebox.theme.builtin.DarkDefaultTheme
import tv.litebox.theme.builtin.BlueSteelTheme

class ThemeManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("litebox_prefs", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** All built-in themes — must be initialized before _currentTheme */
    val builtinThemes: List<ThemeManifest> = listOf(
        DarkDefaultTheme,
        BlueSteelTheme,
    )

    private val _currentTheme = MutableStateFlow(loadActiveTheme())
    val currentTheme: StateFlow<ThemeManifest> = _currentTheme.asStateFlow()

    fun setTheme(themeId: String) {
        val theme = builtinThemes.firstOrNull { it.id == themeId }
            ?: return
        prefs.edit().putString(PREF_ACTIVE_THEME, themeId).apply()
        _currentTheme.value = theme
    }

    suspend fun installThemeFromUrl(url: String): Result<ThemeManifest> = runCatching {
        // TODO: fetch JSON, validate, save to files, reload list
        error("Remote theme install not yet implemented")
    }

    private fun loadActiveTheme(): ThemeManifest {
        val activeId = prefs.getString(PREF_ACTIVE_THEME, DarkDefaultTheme.id)
        return builtinThemes.firstOrNull { it.id == activeId } ?: DarkDefaultTheme
    }

    companion object {
        private const val PREF_ACTIVE_THEME = "active_theme_id"
    }
}

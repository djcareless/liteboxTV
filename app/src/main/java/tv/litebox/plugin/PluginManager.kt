package tv.litebox.plugin

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.litebox.LiteBoxApp
import tv.litebox.data.db.entity.PluginEntity
import tv.litebox.domain.model.InstalledPlugin
import tv.litebox.domain.model.PluginManifest
import java.io.File

/**
 * PluginManager — install, remove, enable/disable plugins.
 *
 * Plugins are identified by a JSON manifest URL.
 * The manifest is fetched, validated, stored in Room, and
 * activated on app restart (or immediately if hot-pluggable).
 *
 * Plugin execution model:
 *   SOURCE plugins → provide URL lists via their entrypoint API
 *   SCRAPER plugins → accept media metadata queries, return enriched data
 *   SUBTITLE plugins → accept title/IMDB id, return subtitle URLs
 */
class PluginManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db get() = LiteBoxApp.instance.database
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder().build()

    /** Observe all installed plugins */
    val plugins: Flow<List<InstalledPlugin>> =
        db.pluginDao().observeAll().map { list -> list.map { it.toDomain() } }

    /** Observe only enabled plugins */
    val enabledPlugins: Flow<List<InstalledPlugin>> =
        db.pluginDao().observeEnabled().map { list -> list.map { it.toDomain() } }

    /**
     * Install a plugin from a manifest URL.
     * Fetches the manifest, validates it, stores it.
     * Returns Result with the installed plugin on success.
     */
    suspend fun installFromUrl(manifestUrl: String): Result<InstalledPlugin> = runCatching {
        val request = Request.Builder().url(manifestUrl).get().build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: error("Empty manifest response")
        val manifest = json.decodeFromString<PluginManifest>(body)

        // Basic validation
        require(manifest.id.isNotBlank()) { "Plugin id cannot be blank" }
        require(manifest.name.isNotBlank()) { "Plugin name cannot be blank" }
        require(manifest.entrypoint.isNotBlank()) { "Plugin entrypoint cannot be blank" }

        val installDir = File(context.filesDir, "plugins/${manifest.id}")
        installDir.mkdirs()

        val installed = InstalledPlugin(
            manifest = manifest,
            installPath = installDir.absolutePath,
            enabled = true,
            installedAt = System.currentTimeMillis(),
            settings = emptyMap(),
        )

        db.pluginDao().upsert(PluginEntity.from(installed))
        installed
    }

    suspend fun uninstall(pluginId: String) {
        db.pluginDao().deleteById(pluginId)
        // Also remove cached media from this plugin
        db.mediaItemDao().deleteByPlugin(pluginId)
        File(context.filesDir, "plugins/$pluginId").deleteRecursively()
    }

    suspend fun setEnabled(pluginId: String, enabled: Boolean) {
        db.pluginDao().setEnabled(pluginId, enabled)
    }

    suspend fun updateSettings(pluginId: String, settings: Map<String, String>) {
        db.pluginDao().updateSettings(pluginId, settings)
    }
}

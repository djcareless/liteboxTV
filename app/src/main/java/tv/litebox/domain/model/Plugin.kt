package tv.litebox.domain.model

import kotlinx.serialization.Serializable

/**
 * LiteBox Plugin Manifest v1
 *
 * A plugin is a self-describing JSON bundle that can provide:
 *  - media sources (video streams, music, IPTV)
 *  - metadata scrapers
 *  - subtitle providers
 *
 * Manifest file: plugin.json (served from plugin repo or bundled in .lbplugin zip)
 */
@Serializable
data class PluginManifest(
    val id: String,                     // reverse-domain: com.example.myplugin
    val name: String,
    val version: String,                // semver: 1.0.0
    val author: String,
    val description: String,
    val type: PluginType,
    val minAppVersion: String = "0.1.0",
    val iconUrl: String? = null,
    val bannerUrl: String? = null,
    val settingsSchema: List<PluginSetting> = emptyList(),
    val provides: List<String> = emptyList(), // ["video", "audio", "metadata", "subtitles"]
    val entrypoint: String,             // URL to main plugin script / API base
    val apiVersion: Int = 1,
    val license: String = "Unknown",
    val repositoryUrl: String? = null,
    val changelogUrl: String? = null,
)

@Serializable
enum class PluginType {
    SOURCE,         // provides media listings
    SCRAPER,        // provides metadata for local files
    SUBTITLE,       // provides subtitles
    UTILITY,        // misc tools
}

@Serializable
data class PluginSetting(
    val key: String,
    val label: String,
    val type: SettingType,
    val defaultValue: String? = null,
    val required: Boolean = false,
    val hint: String? = null,
)

@Serializable
enum class SettingType {
    TEXT, PASSWORD, BOOLEAN, SELECT, NUMBER
}

/** An installed plugin record (stored in DB) */
data class InstalledPlugin(
    val manifest: PluginManifest,
    val installPath: String,
    val enabled: Boolean,
    val installedAt: Long,
    val settings: Map<String, String>,
)

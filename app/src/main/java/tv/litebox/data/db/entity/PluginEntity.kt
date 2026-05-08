package tv.litebox.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import tv.litebox.domain.model.InstalledPlugin
import tv.litebox.domain.model.PluginManifest
import kotlinx.serialization.json.Json

@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val id: String,
    val manifestJson: String,       // full PluginManifest JSON
    val installPath: String,
    val enabled: Boolean,
    val installedAt: Long,
    val settings: Map<String, String>,
) {
    fun toDomain(): InstalledPlugin {
        val manifest = Json.decodeFromString<PluginManifest>(manifestJson)
        return InstalledPlugin(
            manifest = manifest,
            installPath = installPath,
            enabled = enabled,
            installedAt = installedAt,
            settings = settings,
        )
    }

    companion object {
        fun from(p: InstalledPlugin) = PluginEntity(
            id = p.manifest.id,
            manifestJson = Json.encodeToString(PluginManifest.serializer(), p.manifest),
            installPath = p.installPath,
            enabled = p.enabled,
            installedAt = p.installedAt,
            settings = p.settings,
        )
    }
}

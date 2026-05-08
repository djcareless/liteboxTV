package tv.litebox.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import tv.litebox.domain.model.MediaSource
import tv.litebox.domain.model.SourceType

@Entity(tableName = "media_sources")
data class MediaSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val path: String,
    val pluginId: String?,
    val scanEnabled: Boolean,
) {
    fun toDomain() = MediaSource(
        id = id, name = name,
        type = SourceType.valueOf(type),
        path = path, pluginId = pluginId,
        scanEnabled = scanEnabled,
    )

    companion object {
        fun from(s: MediaSource) = MediaSourceEntity(
            id = s.id, name = s.name, type = s.type.name,
            path = s.path, pluginId = s.pluginId, scanEnabled = s.scanEnabled,
        )
    }
}

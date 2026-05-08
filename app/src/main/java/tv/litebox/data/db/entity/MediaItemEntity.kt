package tv.litebox.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import tv.litebox.domain.model.MediaItem
import tv.litebox.domain.model.MediaType

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val uri: String,
    val thumbnailUrl: String?,
    val backdropUrl: String?,
    val description: String?,
    val year: Int?,
    val duration: Long?,
    val rating: Float?,
    val genres: List<String>,
    val sourcePlugin: String?,
    val resumePosition: Long,
    val watched: Boolean,
    val addedAt: Long,
) {
    fun toDomain() = MediaItem(
        id = id, title = title,
        type = MediaType.valueOf(type),
        uri = uri, thumbnailUrl = thumbnailUrl,
        backdropUrl = backdropUrl, description = description,
        year = year, duration = duration, rating = rating,
        genres = genres, sourcePlugin = sourcePlugin,
        resumePosition = resumePosition, watched = watched,
        addedAt = addedAt,
    )

    companion object {
        fun from(item: MediaItem) = MediaItemEntity(
            id = item.id, title = item.title,
            type = item.type.name, uri = item.uri,
            thumbnailUrl = item.thumbnailUrl, backdropUrl = item.backdropUrl,
            description = item.description, year = item.year,
            duration = item.duration, rating = item.rating,
            genres = item.genres, sourcePlugin = item.sourcePlugin,
            resumePosition = item.resumePosition, watched = item.watched,
            addedAt = item.addedAt,
        )
    }
}

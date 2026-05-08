package tv.litebox.domain.model

import kotlinx.serialization.Serializable

/** A single media item — movie, episode, song, photo, etc. */
@Serializable
data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val uri: String,                    // playback URI (file, http, smb, plugin://)
    val thumbnailUrl: String? = null,
    val backdropUrl: String? = null,
    val description: String? = null,
    val year: Int? = null,
    val duration: Long? = null,         // ms
    val rating: Float? = null,
    val genres: List<String> = emptyList(),
    val sourcePlugin: String? = null,   // plugin id that provided this item
    val resumePosition: Long = 0L,      // ms
    val watched: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
)

@Serializable
enum class MediaType {
    MOVIE, TV_EPISODE, TV_SHOW, MUSIC, LIVE, IPTV_CHANNEL, PHOTO, UNKNOWN
}

@Serializable
data class MediaSource(
    val id: String,
    val name: String,
    val type: SourceType,
    val path: String,                   // local path or URL
    val pluginId: String? = null,
    val scanEnabled: Boolean = true,
)

@Serializable
enum class SourceType {
    LOCAL, SMB, NFS, HTTP, PLUGIN, JELLYFIN, PLEX, EMBY
}

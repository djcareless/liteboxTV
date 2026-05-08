package tv.litebox.plugin

/**
 * Contract for scraper-type plugins.
 * Implementations fetch metadata, artwork, and episode info from external APIs
 * like TMDB, TVDB, or any metadata source.
 */
interface ScraperPlugin {
    /** Unique plugin manifest ID this scraper handles */
    val pluginId: String

    /** Search for media by query. Optional type filter: "movie", "tv", "person" */
    suspend fun search(query: String, type: String? = null): List<ScrapeResult>

    /** Get full details for a media item by its external ID */
    suspend fun getDetails(id: String, type: String): ScrapeDetails?

    /** Get available artwork (posters, backdrops, logos) */
    suspend fun getArtwork(id: String, type: String): ArtworkResult?

    /** Get episode list for a TV show season */
    suspend fun getSeason(showId: String, seasonNumber: Int): List<EpisodeInfo>?
}

data class ScrapeResult(
    val id: String,
    val title: String,
    val year: Int?,
    val type: String,       // "movie", "tv", "person"
    val score: Float?,      // 0.0 - 10.0
    val overview: String?,
    val posterUrl: String?,
)

data class ScrapeDetails(
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val year: Int?,
    val rating: Float?,
    val voteCount: Int?,
    val genres: List<String>,
    val runtime: Int?,      // minutes
    val status: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val externalIds: Map<String, String>,  // imdb_id, tvdb_id, etc.
)

data class ArtworkResult(
    val posters: List<String>,
    val backdrops: List<String>,
    val logos: List<String>,
)

data class EpisodeInfo(
    val number: Int,
    val title: String,
    val overview: String?,
    val stillUrl: String?,
    val airDate: String?,
    val rating: Float?,
)

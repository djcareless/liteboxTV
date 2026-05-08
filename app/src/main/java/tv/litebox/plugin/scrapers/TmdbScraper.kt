package tv.litebox.plugin.scrapers

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import tv.litebox.plugin.*

/**
 * TMDB scraper implementation.
 * Uses the TMDB v3 API to fetch movie/TV metadata and artwork.
 * Auth via api_key query parameter (from plugin settings).
 * Uses OkHttp for HTTP calls (already a project dependency).
 */
class TmdbScraper(
    override val pluginId: String = "tv.litebox.plugin.tmdb",
    private val apiKey: String,
    private val language: String = "en",
    private val includeAdult: Boolean = false,
    private val baseUrl: String = "https://api.themoviedb.org/3",
    private val imageBaseUrl: String = "https://image.tmdb.org/t/p",
) : ScraperPlugin {

    @Serializable
    data class TmdbSearchResponse(
        val results: List<JsonObject>,
        val total_results: Int = 0,
    )

    @Serializable
    data class TmdbMovieResult(
        val id: Int,
        val title: String? = null,
        val name: String? = null,
        val release_date: String? = null,
        val first_air_date: String? = null,
        val media_type: String? = null,
        val vote_average: Float? = null,
        val overview: String? = null,
        val poster_path: String? = null,
    )

    @Serializable
    data class TmdbMovieDetails(
        val id: Int,
        val title: String? = null,
        val name: String? = null,
        val original_title: String? = null,
        val original_name: String? = null,
        val overview: String? = null,
        val release_date: String? = null,
        val first_air_date: String? = null,
        val vote_average: Float? = null,
        val vote_count: Int? = null,
        val genres: List<TmdbGenre> = emptyList(),
        val runtime: Int? = null,
        val episode_run_time: List<Int>? = null,
        val status: String? = null,
        val poster_path: String? = null,
        val backdrop_path: String? = null,
        val imdb_id: String? = null,
        val external_ids: TmdbExternalIds? = null,
    )

    @Serializable
    data class TmdbGenre(val id: Int, val name: String)

    @Serializable
    data class TmdbExternalIds(
        val imdb_id: String? = null,
        val tvdb_id: Int? = null,
        val tvrage_id: Int? = null,
    )

    @Serializable
    data class TmdbSeason(
        val episodes: List<TmdbEpisode> = emptyList(),
    )

    @Serializable
    data class TmdbEpisode(
        val episode_number: Int,
        val name: String,
        val overview: String? = null,
        val still_path: String? = null,
        val air_date: String? = null,
        val vote_average: Float? = null,
    )

    @Serializable
    data class TmdbImages(
        val posters: List<TmdbImage> = emptyList(),
        val backdrops: List<TmdbImage> = emptyList(),
        val logos: List<TmdbImage> = emptyList(),
    )

    @Serializable
    data class TmdbImage(
        val file_path: String,
        val aspect_ratio: Float? = null,
        val vote_average: Float? = null,
    )

    private val client = okhttp3.OkHttpClient()

    private fun imageUrl(path: String?, size: String = "w500"): String? =
        path?.let { "$imageBaseUrl/$size$it" }

    private fun encode(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")

    private fun get(url: String): String {
        val request = okhttp3.Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            return response.body?.string() ?: ""
        }
    }

    override suspend fun search(query: String, type: String?): List<ScrapeResult> {
        val endpoint = when (type) {
            "movie" -> "$baseUrl/search/movie"
            "tv" -> "$baseUrl/search/tv"
            else -> "$baseUrl/search/multi"
        }
        val response = get("$endpoint?api_key=$apiKey&query=${encode(query)}&language=$language&include_adult=$includeAdult")
        val json = Json.decodeFromString<TmdbSearchResponse>(response)
        return json.results.mapNotNull { item ->
            val result = try { Json.decodeFromString<TmdbMovieResult>(item.toString()) } catch (_: Exception) { return@mapNotNull null }
            val mediaType = result.media_type ?: type ?: "movie"
            if (mediaType == "person") return@mapNotNull null
            ScrapeResult(
                id = result.id.toString(),
                title = result.title ?: result.name ?: return@mapNotNull null,
                year = (result.release_date ?: result.first_air_date)?.take(4)?.toIntOrNull(),
                type = mediaType,
                score = result.vote_average,
                overview = result.overview,
                posterUrl = imageUrl(result.poster_path, "w342"),
            )
        }
    }

    override suspend fun getDetails(id: String, type: String): ScrapeDetails? {
        val endpoint = when (type) {
            "tv" -> "$baseUrl/tv/$id"
            else -> "$baseUrl/movie/$id"
        }
        val response = get("$endpoint?api_key=$apiKey&language=$language&append_to_response=external_ids")
        val details = try { Json.decodeFromString<TmdbMovieDetails>(response) } catch (_: Exception) { return null }
        return ScrapeDetails(
            title = details.title ?: details.name ?: "",
            originalTitle = details.original_title ?: details.original_name,
            overview = details.overview,
            year = (details.release_date ?: details.first_air_date)?.take(4)?.toIntOrNull(),
            rating = details.vote_average,
            voteCount = details.vote_count,
            genres = details.genres.map { it.name },
            runtime = details.runtime ?: details.episode_run_time?.firstOrNull(),
            status = details.status,
            posterUrl = imageUrl(details.poster_path),
            backdropUrl = imageUrl(details.backdrop_path, "w1280"),
            externalIds = buildMap {
                details.imdb_id?.let { put("imdb_id", it) }
                details.external_ids?.imdb_id?.let { put("imdb_id", it) }
                details.external_ids?.tvdb_id?.let { put("tvdb_id", it.toString()) }
            },
        )
    }

    override suspend fun getArtwork(id: String, type: String): ArtworkResult? {
        val endpoint = when (type) {
            "tv" -> "$baseUrl/tv/$id/images"
            else -> "$baseUrl/movie/$id/images"
        }
        val response = get("$endpoint?api_key=$apiKey")
        val images = try { Json.decodeFromString<TmdbImages>(response) } catch (_: Exception) { return null }
        return ArtworkResult(
            posters = images.posters.mapNotNull { imageUrl(it.file_path, "w500") },
            backdrops = images.backdrops.mapNotNull { imageUrl(it.file_path, "w1280") },
            logos = images.logos.mapNotNull { imageUrl(it.file_path, "w500") },
        )
    }

    override suspend fun getSeason(showId: String, seasonNumber: Int): List<EpisodeInfo>? {
        val response = get("$baseUrl/tv/$showId/season/$seasonNumber?api_key=$apiKey&language=$language")
        val season = try { Json.decodeFromString<TmdbSeason>(response) } catch (_: Exception) { return null }
        return season.episodes.map { ep ->
            EpisodeInfo(
                number = ep.episode_number,
                title = ep.name,
                overview = ep.overview,
                stillUrl = imageUrl(ep.still_path, "w500"),
                airDate = ep.air_date,
                rating = ep.vote_average,
            )
        }
    }
}

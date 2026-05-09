package tv.litebox.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import tv.litebox.LiteBoxApp
import tv.litebox.data.db.entity.MediaItemEntity
import tv.litebox.domain.model.MediaItem
import tv.litebox.domain.model.MediaType
import tv.litebox.plugin.ScraperPlugin

data class HomeUiState(
    val continueWatching: List<MediaItem> = emptyList(),
    val recentMovies: List<MediaItem> = emptyList(),
    val recentTvShows: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val isScraping: Boolean = false,
)

class HomeViewModel : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        /** Max concurrent scraper API requests to avoid throttling */
        private const val MAX_CONCURRENT_SCRAPES = 2
        /** Don't re-scrape items scraped within the last 24 hours */
        private const val SCRAPE_FRESHNESS_MS = 24 * 60 * 60 * 1000L
    }

    private val app = LiteBoxApp.instance
    private val db = app.database
    private val scraperFactory = app.scraperPluginFactory

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Semaphore to rate-limit concurrent scraper requests */
    private val scrapeSemaphore = Semaphore(MAX_CONCURRENT_SCRAPES)

    /** Track IDs already enqueued for scraping to avoid duplicates */
    private val enqueuedForScrape = mutableSetOf<String>()

    init {
        // ── 1. Observe Room flows and emit local data immediately ──────────
        viewModelScope.launch {
            combine(
                db.mediaItemDao().observeContinueWatching(),
                db.mediaItemDao().observeByType(MediaType.MOVIE.name),
                db.mediaItemDao().observeByType(MediaType.TV_SHOW.name),
            ) { continueWatching, movies, tvShows ->
                HomeUiState(
                    continueWatching = continueWatching.map { it.toDomain() },
                    recentMovies = movies.take(20).map { it.toDomain() },
                    recentTvShows = tvShows.take(20).map { it.toDomain() },
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.value = state

                // ── 2. Trigger async enrichment for un-scraped items ────────
                val allItems = state.continueWatching + state.recentMovies + state.recentTvShows
                val needsScrape = allItems.filter { needsScrape(it) }
                if (needsScrape.isNotEmpty()) {
                    triggerEnrichment(needsScrape)
                }
            }
        }

        // ── 3. Create scrapers from enabled plugins ─────────────────────
        viewModelScope.launch {
            scraperFactory.createScrapers()
        }
    }

    /**
     * Check if a [MediaItem] needs scraper enrichment.
     * Items are skipped if they already have scraped metadata that is fresh
     * (within [SCRAPE_FRESHNESS_MS]) or if they're already queued.
     */
    private fun needsScrape(item: MediaItem): Boolean {
        if (item.id in enqueuedForScrape) return false
        // Already scraped and fresh — skip
        if (item.scrapedAt != null && item.posterUrl != null) {
            val age = System.currentTimeMillis() - item.scrapedAt
            if (age < SCRAPE_FRESHNESS_MS) return false
        }
        return true
    }

    /**
     * Enqueue items for async scraper enrichment.
     * Each item is launched independently so one failure doesn't block others.
     * The [scrapeSemaphore] limits concurrency to [MAX_CONCURRENT_SCRAPES].
     */
    private fun triggerEnrichment(items: List<MediaItem>) {
        val scrapers = scraperFactory.allScrapers
        if (scrapers.isEmpty()) return

        _uiState.value = _uiState.value.copy(isScraping = true)

        for (item in items) {
            if (!enqueuedForScrape.add(item.id)) continue
            viewModelScope.launch(Dispatchers.IO) {
                enrichItem(item, scrapers)
            }
        }
    }

    /**
     * Search active scrapers for [item], pick the best match, fetch details,
     * and persist enriched metadata back to Room.
     */
    private suspend fun enrichItem(item: MediaItem, scrapers: List<ScraperPlugin>) {
        try {
            scrapeSemaphore.acquire()
            val cleanTitle = sanitizeTitle(item.title)
            val scraperType = mapMediaType(item.type)

            for (scraper in scrapers) {
                try {
                    val results = scraper.search(cleanTitle, scraperType)
                    val match = findBestMatch(cleanTitle, item.year, results) ?: continue

                    // Fetch full details + artwork
                    val details = scraper.getDetails(match.id, match.type)
                    val artwork = scraper.getArtwork(match.id, match.type)

                    // Pick best poster: details → artwork → search result
                    val posterUrl = details?.posterUrl
                        ?: artwork?.posters?.firstOrNull()
                        ?: match.posterUrl

                    val backdropUrl = details?.backdropUrl
                        ?: artwork?.backdrops?.firstOrNull()

                    // Persist to Room — the Flow will pick up the change and re-emit
                    db.mediaItemDao().updateScraperMetadata(
                        id = item.id,
                        posterUrl = posterUrl,
                        backdropUrl = backdropUrl ?: item.backdropUrl,
                        rating = details?.rating ?: match.score,
                        overview = details?.overview ?: match.overview,
                        genres = details?.genres ?: emptyList(),
                        externalId = match.id,
                        scrapedAt = System.currentTimeMillis(),
                    )
                    Log.d(TAG, "Enriched '${item.title}' → poster=$posterUrl rating=${details?.rating}")
                    return // success — done with this item
                } catch (e: Exception) {
                    Log.w(TAG, "Scraper ${scraper.pluginId} failed for '${item.title}': ${e.message}")
                }
            }
            // No scraper matched — mark as scraped with null data to avoid retry spam
            db.mediaItemDao().updateScraperMetadata(
                id = item.id,
                posterUrl = null,
                backdropUrl = item.backdropUrl,
                rating = item.rating,
                overview = item.overview,
                genres = item.genres,
                externalId = null,
                scrapedAt = System.currentTimeMillis(),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error enriching '${item.title}': ${e.message}")
        } finally {
            scrapeSemaphore.release()
            enqueuedForScrape.remove(item.id)
            // Clear isScraping flag when queue empties
            if (enqueuedForScrape.isEmpty()) {
                _uiState.value = _uiState.value.copy(isScraping = false)
            }
        }
    }

    // ── Matching helpers ────────────────────────────────────────

    /**
     * Find the best matching [ScrapeResult] for the given title.
     * Uses a simple fuzzy approach: exact match > contains match > first result.
     * Year match is used as a tiebreaker.
     */
    private fun findBestMatch(
        title: String,
        year: Int?,
        results: List<tv.litebox.plugin.ScrapeResult>,
    ): tv.litebox.plugin.ScrapeResult? {
        if (results.isEmpty()) return null

        val normalizedTitle = title.lowercase().trim()

        // 1. Exact title + year match
        if (year != null) {
            results.find {
                it.title.lowercase().trim() == normalizedTitle && it.year == year
            }?.let { return it }
        }

        // 2. Exact title match
        results.find { it.title.lowercase().trim() == normalizedTitle }?.let { return it }

        // 3. Title contains the query (or vice versa)
        results.find { normalizedTitle in it.title.lowercase() || it.title.lowercase() in normalizedTitle }
            ?.let { return it }

        // 4. Levenshtein-based: pick the result with smallest edit distance
        return results.minByOrNull { levenshtein(normalizedTitle, it.title.lowercase().trim()) }
    }

    /** Simple Levenshtein distance implementation for fuzzy matching. */
    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // deletion
                    dp[i][j - 1] + 1,       // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[a.length][b.length]
    }

    /** Strip common noise from file-derived titles. */
    private fun sanitizeTitle(title: String): String {
        return title
            .replace(Regex("""\(\d{4}\)"""), "")   // remove (2024)
            .replace(Regex("""\[\w+]"""), "")       // remove [group]
            .replace(Regex("""\.(mkv|mp4|avi|wmv|flv)$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[._\-]+"""), " ")
            .trim()
    }

    /** Map our MediaType to scraper type strings. */
    private fun mapMediaType(type: MediaType): String? = when (type) {
        MediaType.MOVIE -> "movie"
        MediaType.TV_SHOW, MediaType.TV_EPISODE -> "tv"
        else -> null  // let scraper decide
    }
}

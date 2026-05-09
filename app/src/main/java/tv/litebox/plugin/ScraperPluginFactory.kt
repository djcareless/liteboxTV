package tv.litebox.plugin

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.litebox.domain.model.InstalledPlugin
import tv.litebox.domain.model.PluginManifest
import tv.litebox.domain.model.PluginType
import tv.litebox.plugin.scrapers.TmdbScraper

/**
 * Creates and caches [ScraperPlugin] instances by reading API keys from stored
 * plugin settings in Room via [PluginManager].
 *
 * - Scraper instances are cached by plugin ID and only recreated after [refresh].
 * - Scrapers whose required API keys are missing or empty are silently skipped.
 * - All public functions that touch the cache are thread-safe via a [Mutex].
 */
class ScraperPluginFactory(private val pluginManager: PluginManager) {

    /** Known scraper plugin IDs */
    private val registry = setOf(
        "tv.litebox.plugin.tmdb",
        // "tv.litebox.plugin.tvdb",
        // "tv.litebox.plugin.omdb",
    )

    /** Cached scraper instances keyed by plugin ID */
    private val cache = mutableMapOf<String, ScraperPlugin>()

    /** Mutex guarding concurrent access to [cache] */
    private val mutex = Mutex()

    // ── Query helpers (cache-only, no suspend) ──────────────────────

    /** Get a cached scraper by plugin ID, or null if not created / missing key. */
    fun getScraper(pluginId: String): ScraperPlugin? = cache[pluginId]

    /** All currently cached scraper instances. */
    val allScrapers: List<ScraperPlugin>
        get() = cache.values.toList()

    // ── Static helpers ──────────────────────────────────────────────

    /** Check if a plugin manifest is a known scraper */
    fun isScraper(manifest: PluginManifest): Boolean =
        manifest.type.name == "SCRAPER" && manifest.id in registry

    /** Get all registered scraper plugin IDs */
    fun registeredScrapers(): Set<String> = registry

    // ── Cache management ────────────────────────────────────────────

    /**
     * Create (or re-create) a single scraper from [plugin] and cache it.
     * Returns the created scraper, or null if the required API key is missing.
     */
    fun create(plugin: InstalledPlugin): ScraperPlugin? {
        val scraper = instantiate(plugin) ?: return null
        cache[plugin.manifest.id] = scraper
        return scraper
    }

    /**
     * Read all enabled plugins from Room, build scrapers for every known
     * scraper plugin that has its required API key configured, and replace
     * the entire cache.
     *
     * Must be called from a coroutine (suspend).
     */
    suspend fun createScrapers() {
        mutex.withLock {
            cache.clear()
            val plugins = pluginManager.getEnabledPlugins()
            for (plugin in plugins) {
                if (plugin.manifest.id !in registry) continue
                if (plugin.manifest.type != PluginType.SCRAPER) continue
                instantiate(plugin)?.let { cache[plugin.manifest.id] = it }
            }
        }
    }

    /**
     * Invalidate the cache so the next access will re-read from Room.
     * Call this after settings are updated via [PluginManager.updateSettings].
     */
    suspend fun refresh() {
        mutex.withLock {
            cache.clear()
            createScrapersInternal()
        }
    }

    // ── Internals ───────────────────────────────────────────────────

    /**
     * Internal non-synchronized version of [createScrapers].
     * Caller must hold [mutex].
     */
    private suspend fun createScrapersInternal() {
        val plugins = pluginManager.getEnabledPlugins()
        for (plugin in plugins) {
            if (plugin.manifest.id !in registry) continue
            if (plugin.manifest.type != PluginType.SCRAPER) continue
            instantiate(plugin)?.let { cache[plugin.manifest.id] = it }
        }
    }

    /**
     * Instantiate a [ScraperPlugin] from an [InstalledPlugin].
     * Returns null when required settings (e.g. API key) are missing or blank.
     */
    private fun instantiate(plugin: InstalledPlugin): ScraperPlugin? {
        val settings = plugin.settings
        return when (plugin.manifest.id) {
            "tv.litebox.plugin.tmdb" -> {
                val apiKey = settings["api_key"]?.trim()
                if (apiKey.isNullOrBlank()) return null
                TmdbScraper(
                    apiKey = apiKey,
                    language = settings["language"] ?: "en",
                    includeAdult = settings["include_adult"]?.toBooleanStrictOrNull() ?: false,
                )
            }
            // Add more scrapers here as they're implemented
            else -> null
        }
    }
}

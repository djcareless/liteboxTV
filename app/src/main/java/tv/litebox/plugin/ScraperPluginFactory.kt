package tv.litebox.plugin

import tv.litebox.domain.model.InstalledPlugin
import tv.litebox.domain.model.PluginManifest
import tv.litebox.plugin.scrapers.TmdbScraper

/**
 * Routes plugin manifests to the correct ScraperPlugin implementation.
 * Add new scrapers here as they're built.
 */
object ScraperPluginFactory {

    /** Known scraper plugin IDs */
    private val registry = setOf(
        "tv.litebox.plugin.tmdb",
        // "tv.litebox.plugin.tvdb",
        // "tv.litebox.plugin.omdb",
    )

    /** Check if a plugin manifest is a known scraper */
    fun isScraper(manifest: PluginManifest): Boolean =
        manifest.type.name == "SCRAPER" && manifest.id in registry

    /** Create a ScraperPlugin instance from an installed plugin */
    fun create(plugin: InstalledPlugin): ScraperPlugin? {
        return when (plugin.manifest.id) {
            "tv.litebox.plugin.tmdb" -> TmdbScraper(
                apiKey = plugin.settings["api_key"] ?: return null,
                language = plugin.settings["language"] ?: "en",
                includeAdult = plugin.settings["include_adult"]?.toBooleanStrictOrNull() ?: false,
            )
            // Add more scrapers here as they're implemented
            else -> null
        }
    }

    /** Get all registered scraper plugin IDs */
    fun registeredScrapers(): Set<String> = registry
}

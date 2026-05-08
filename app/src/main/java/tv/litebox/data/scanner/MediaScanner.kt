package tv.litebox.data.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.litebox.data.scanner.FileExtensions.AUDIO_EXTENSIONS
import tv.litebox.data.scanner.FileExtensions.PHOTO_EXTENSIONS
import tv.litebox.data.scanner.FileExtensions.VIDEO_EXTENSIONS
import tv.litebox.domain.model.MediaItem
import tv.litebox.domain.model.MediaSource
import tv.litebox.domain.model.MediaType
import tv.litebox.domain.model.SourceType
import java.io.File
import java.util.UUID

class MediaScanner {

    private val okHttpClient: OkHttpClient by lazy { OkHttpClient() }

    /**
     * Scan [source] and emit [ScanProgress] events as files are discovered.
     */
    fun scan(source: MediaSource): Flow<ScanProgress> = flow {
        emit(ScanProgress.Started)
        try {
            val items = mutableListOf<MediaItem>()
            when (source.type) {
                SourceType.LOCAL -> scanLocal(source, items) { found, current ->
                    emit(ScanProgress.Progress(found, current))
                }
                SourceType.HTTP -> scanHttp(source.path, source, items) { found, current ->
                    emit(ScanProgress.Progress(found, current))
                }
                else -> {
                    emit(ScanProgress.Error("SourceType ${source.type} is not supported by MediaScanner"))
                    return@flow
                }
            }
            emit(ScanProgress.Complete(items))
        } catch (e: Exception) {
            emit(ScanProgress.Error(e.message ?: "Unknown error during scan"))
        }
    }.flowOn(Dispatchers.IO)

    // -------------------------------------------------------------------------
    // LOCAL scanning
    // -------------------------------------------------------------------------

    private suspend fun scanLocal(
        source: MediaSource,
        items: MutableList<MediaItem>,
        onProgress: suspend (Int, String) -> Unit,
    ) {
        val root = File(source.path)
        if (!root.exists()) {
            throw IllegalArgumentException("Path does not exist: ${source.path}")
        }
        walkLocal(root, source, items, onProgress)
    }

    private suspend fun walkLocal(
        dir: File,
        source: MediaSource,
        items: MutableList<MediaItem>,
        onProgress: suspend (Int, String) -> Unit,
    ) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                walkLocal(child, source, items, onProgress)
            } else {
                val ext = child.extension.lowercase()
                val mediaType = extToMediaType(ext) ?: continue   // skip non-media files
                val item = MediaItem(
                    id = UUID.randomUUID().toString(),
                    title = cleanTitle(child.nameWithoutExtension),
                    type = mediaType,
                    uri = "file://${child.absolutePath}",
                    duration = null,
                    addedAt = System.currentTimeMillis(),
                )
                items += item
                onProgress(items.size, child.name)
            }
        }
    }

    // -------------------------------------------------------------------------
    // HTTP directory-index scraping  (Apache / Nginx autoindex)
    // -------------------------------------------------------------------------

    private suspend fun scanHttp(
        url: String,
        source: MediaSource,
        items: MutableList<MediaItem>,
        onProgress: suspend (Int, String) -> Unit,
    ) {
        val normalised = url.trimEnd('/')
        val html = fetchHtml(normalised) ?: throw IllegalStateException("Could not fetch $normalised")
        scrapeLinks(html, normalised, source, items, onProgress)
    }

    private suspend fun scrapeLinks(
        html: String,
        baseUrl: String,
        source: MediaSource,
        items: MutableList<MediaItem>,
        onProgress: suspend (Int, String) -> Unit,
    ) {
        // Match href="..." values from anchor tags (autoindex uses simple href attributes)
        val hrefRegex = Regex("""href="([^"?#]+)"""")
        for (match in hrefRegex.findAll(html)) {
            val href = match.groupValues[1]
            // Skip parent/root links
            if (href == "../" || href == "./") continue

            val fullUrl = when {
                href.startsWith("http://") || href.startsWith("https://") -> href
                href.startsWith("/") -> {
                    val proto = baseUrl.substringBefore("://")
                    val host = baseUrl.substringAfter("://").substringBefore("/")
                    "$proto://$host$href"
                }
                else -> "$baseUrl/$href"
            }.trimEnd('/')

            if (href.endsWith("/")) {
                // Recurse into sub-directory
                val subHtml = fetchHtml(fullUrl) ?: continue
                scrapeLinks(subHtml, fullUrl, source, items, onProgress)
            } else {
                val filename = href.substringAfterLast("/")
                val ext = filename.substringAfterLast('.', "").lowercase()
                val mediaType = extToMediaType(ext) ?: continue
                val item = MediaItem(
                    id = UUID.randomUUID().toString(),
                    title = cleanTitle(filename.substringBeforeLast('.')),
                    type = mediaType,
                    uri = fullUrl,
                    duration = null,
                    addedAt = System.currentTimeMillis(),
                )
                items += item
                onProgress(items.size, filename)
            }
        }
    }

    private fun fetchHtml(url: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (_: Exception) {
            null
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun extToMediaType(ext: String): MediaType? = when {
        ext in VIDEO_EXTENSIONS -> MediaType.MOVIE
        ext in AUDIO_EXTENSIONS -> MediaType.MUSIC
        ext in PHOTO_EXTENSIONS -> MediaType.PHOTO
        else -> null
    }

    /**
     * Convert a raw filename (no extension) into a human-readable title.
     * e.g. "the_dark_knight.2008" → "The Dark Knight 2008"
     */
    private fun cleanTitle(raw: String): String =
        raw.replace(Regex("[._]"), " ")
            .trim()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
}

package tv.litebox.data.scanner

import tv.litebox.domain.model.MediaItem

sealed class ScanProgress {
    /** Emitted immediately when scanning starts for a source. */
    data object Started : ScanProgress()

    /** Emitted each time a new file is discovered. */
    data class Progress(val found: Int, val current: String) : ScanProgress()

    /** Emitted once when the scan is fully complete. */
    data class Complete(val items: List<MediaItem>) : ScanProgress()

    /** Emitted if an unrecoverable error occurs. */
    data class Error(val message: String) : ScanProgress()
}

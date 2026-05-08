package tv.litebox.data.scanner

object FileExtensions {
    val VIDEO_EXTENSIONS = setOf(
        "mkv", "mp4", "avi", "m4v", "mov", "wmv", "flv", "webm",
        "ts", "m2ts", "mts", "vob", "ogv", "3gp", "3g2", "divx",
        "xvid", "rmvb", "rm", "asf", "mpg", "mpeg", "m2v", "f4v",
    )

    val AUDIO_EXTENSIONS = setOf(
        "mp3", "flac", "m4a", "ogg", "aac", "wav", "wma", "opus",
        "ape", "alac", "aiff", "aif", "dsd", "dsf", "dff", "wv",
        "mka", "ac3", "dts", "amr", "mid", "midi",
    )

    val PHOTO_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "webp", "gif", "bmp", "tiff", "tif",
        "heic", "heif", "avif", "svg", "raw", "cr2", "nef", "arw",
    )

    val SUBTITLE_EXTENSIONS = setOf(
        "srt", "sub", "ass", "ssa", "vtt", "idx", "sup", "smi",
        "lrc", "dfxp", "ttml",
    )
}

package tv.litebox.domain.model

import kotlinx.serialization.Serializable

/**
 * LiteBox Theme/Skin Manifest v1
 *
 * A theme is a JSON file that defines colors, typography scale,
 * corner radii, spacing, and optional asset overrides.
 * Themes are installable from URLs or bundled in the app.
 */
@Serializable
data class ThemeManifest(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val preview: String? = null,    // URL to preview image
    val colors: ThemeColors,
    val typography: ThemeTypography = ThemeTypography(),
    val shape: ThemeShape = ThemeShape(),
)

@Serializable
data class ThemeColors(
    // Background layers
    val background: String = "#0D0D0D",
    val surface: String = "#1A1A1A",
    val surfaceVariant: String = "#242424",
    val surfaceBright: String = "#2E2E2E",

    // Brand
    val primary: String = "#E50914",         // Netflix-red default
    val primaryContainer: String = "#8B0000",
    val onPrimary: String = "#FFFFFF",

    // Text
    val onBackground: String = "#F5F5F5",
    val onSurface: String = "#E0E0E0",
    val onSurfaceVariant: String = "#9E9E9E",

    // Focus indicator (crucial for remote navigation)
    val focus: String = "#FFFFFF",
    val focusBorder: String = "#FFFFFF",

    // Status
    val error: String = "#CF6679",
    val success: String = "#4CAF50",
)

@Serializable
data class ThemeTypography(
    val displayFontFamily: String = "sans-serif",
    val bodyFontFamily: String = "sans-serif",
    val displayScaleFactor: Float = 1.0f,
    val bodyScaleFactor: Float = 1.0f,
)

@Serializable
data class ThemeShape(
    val cardCornerRadius: Int = 8,      // dp
    val buttonCornerRadius: Int = 4,
    val dialogCornerRadius: Int = 12,
)

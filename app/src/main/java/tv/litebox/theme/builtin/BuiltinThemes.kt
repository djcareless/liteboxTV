package tv.litebox.theme.builtin

import tv.litebox.domain.model.ThemeColors
import tv.litebox.domain.model.ThemeManifest
import tv.litebox.domain.model.ThemeShape
import tv.litebox.domain.model.ThemeTypography

/** LiteBox default dark theme */
val DarkDefaultTheme = ThemeManifest(
    id = "litebox.dark",
    name = "Dark",
    version = "1.0.0",
    author = "LiteBox",
    description = "Clean dark theme optimized for TV viewing.",
    colors = ThemeColors(
        background = "#0D0D0D",
        surface = "#1A1A1A",
        surfaceVariant = "#242424",
        surfaceBright = "#2E2E2E",
        primary = "#E50914",
        primaryContainer = "#8B0000",
        onPrimary = "#FFFFFF",
        onBackground = "#F5F5F5",
        onSurface = "#E0E0E0",
        onSurfaceVariant = "#9E9E9E",
        focus = "#FFFFFF",
        focusBorder = "#FFFFFF",
        error = "#CF6679",
        success = "#4CAF50",
    ),
    typography = ThemeTypography(displayScaleFactor = 1.0f),
    shape = ThemeShape(cardCornerRadius = 8),
)

/** Blue Steel — cool blue/grey palette */
val BlueSteelTheme = ThemeManifest(
    id = "litebox.blue-steel",
    name = "Blue Steel",
    version = "1.0.0",
    author = "LiteBox",
    description = "Cool blue-grey palette. Easy on the eyes.",
    colors = ThemeColors(
        background = "#0A0E17",
        surface = "#111827",
        surfaceVariant = "#1E2A3A",
        surfaceBright = "#253347",
        primary = "#3B82F6",
        primaryContainer = "#1D4ED8",
        onPrimary = "#FFFFFF",
        onBackground = "#E2E8F0",
        onSurface = "#CBD5E1",
        onSurfaceVariant = "#94A3B8",
        focus = "#60A5FA",
        focusBorder = "#60A5FA",
        error = "#F87171",
        success = "#34D399",
    ),
    typography = ThemeTypography(displayScaleFactor = 1.0f),
    shape = ThemeShape(cardCornerRadius = 6),
)

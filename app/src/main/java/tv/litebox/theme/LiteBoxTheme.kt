package tv.litebox.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import tv.litebox.domain.model.ThemeManifest

/** Parse a hex color string to Compose Color */
fun String.toComposeColor(): Color {
    val cleaned = removePrefix("#")
    return when (cleaned.length) {
        6 -> Color(("FF$cleaned").toLong(16).toInt())
        8 -> Color(cleaned.toLong(16).toInt())
        else -> Color.Unspecified
    }
}

/** Apply a ThemeManifest as a Jetpack Compose TV MaterialTheme */
@Composable
fun LiteBoxTheme(
    theme: ThemeManifest,
    content: @Composable () -> Unit,
) {
    val colors = darkColorScheme(
        primary = theme.colors.primary.toComposeColor(),
        onPrimary = theme.colors.onPrimary.toComposeColor(),
        primaryContainer = theme.colors.primaryContainer.toComposeColor(),
        background = theme.colors.background.toComposeColor(),
        surface = theme.colors.surface.toComposeColor(),
        surfaceVariant = theme.colors.surfaceVariant.toComposeColor(),
        onBackground = theme.colors.onBackground.toComposeColor(),
        onSurface = theme.colors.onSurface.toComposeColor(),
        onSurfaceVariant = theme.colors.onSurfaceVariant.toComposeColor(),
        error = theme.colors.error.toComposeColor(),
    )

    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}

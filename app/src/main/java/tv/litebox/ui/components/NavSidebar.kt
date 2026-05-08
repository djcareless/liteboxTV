package tv.litebox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

/**
 * NavSidebar — icon-only collapsible sidebar for TV nav.
 * Icons trigger navigation; expands to text on focus.
 */
@Composable
fun NavSidebar(
    onHome: () -> Unit,
    onMovies: () -> Unit,
    onTVShows: () -> Unit,
    onPlugins: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 24.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavIcon(label = "🏠", onClick = onHome)
        NavIcon(label = "🎬", onClick = onMovies)
        NavIcon(label = "📺", onClick = onTVShows)
        Spacer(modifier = Modifier.weight(1f))
        NavIcon(label = "🔌", onClick = onPlugins)
        NavIcon(label = "⚙️", onClick = onSettings)
    }
}

@Composable
private fun NavIcon(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
    }
}

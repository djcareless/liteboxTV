package tv.litebox.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import tv.litebox.LiteBoxApp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val themeManager = LiteBoxApp.instance.themeManager
    val currentTheme by themeManager.currentTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        // Theme section
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        TvLazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(themeManager.builtinThemes) { theme ->
                val selected = theme.id == currentTheme.id
                Card(
                    onClick = { themeManager.setTheme(theme.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(theme.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = theme.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (selected) {
                            Text("✓ Active", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // App info
        Text(
            text = "LiteBox TV · v0.1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

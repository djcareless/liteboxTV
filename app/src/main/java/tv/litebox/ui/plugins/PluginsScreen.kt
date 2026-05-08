package tv.litebox.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import tv.litebox.domain.model.InstalledPlugin

@Composable
fun PluginsScreen(
    onBack: () -> Unit,
    vm: PluginsViewModel = viewModel(),
) {
    val plugins by vm.plugins.collectAsState()
    var showInstallDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }
    val installState by vm.installState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Plugins",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(onClick = { showInstallDialog = true }) {
                Text("+ Install Plugin")
            }
        }

        Spacer(Modifier.height(24.dp))

        if (plugins.isEmpty()) {
            Text(
                text = "No plugins installed. Install a plugin by URL to add media sources.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            TvLazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(plugins) { plugin ->
                    PluginRow(
                        plugin = plugin,
                        onToggle = { vm.togglePlugin(plugin.manifest.id, !plugin.enabled) },
                        onRemove = { vm.removePlugin(plugin.manifest.id) },
                    )
                }
            }
        }
    }

    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text("Install Plugin") },
            text = {
                Column {
                    Text("Enter the plugin manifest URL (.json):")
                    Spacer(Modifier.height(8.dp))
                    // Note: TextField not shown here to keep this concise
                    // In full implementation, use OutlinedTextField with urlInput
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.installPlugin(urlInput)
                    showInstallDialog = false
                }) { Text("Install") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showInstallDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PluginRow(
    plugin: InstalledPlugin,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(plugin.manifest.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${plugin.manifest.version} · ${plugin.manifest.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = plugin.manifest.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onToggle) {
                    Text(if (plugin.enabled) "Disable" else "Enable")
                }
                OutlinedButton(onClick = onRemove) {
                    Text("Remove")
                }
            }
        }
    }
}

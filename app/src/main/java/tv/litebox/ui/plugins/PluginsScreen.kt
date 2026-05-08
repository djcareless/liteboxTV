package tv.litebox.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Surface
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import tv.litebox.domain.model.InstalledPlugin

@Composable
fun PluginsScreen(
    onBack: () -> Unit,
    vm: PluginsViewModel = viewModel(),
) {
    val plugins by vm.plugins.collectAsState()
    val installState by vm.installState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showInstallDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Plugins",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(onClick = { showInstallDialog = true }) {
                Text("+ Install URL")
            }
        }

        Spacer(Modifier.height(20.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onFocus = { selectedTab = 0 },
                onClick = { selectedTab = 0 },
            ) { Text("Installed") }
            Tab(
                selected = selectedTab == 1,
                onFocus = { selectedTab = 1 },
                onClick = { selectedTab = 1 },
            ) { Text("Browse") }
        }

        Spacer(Modifier.height(16.dp))
        InstallStateBanner(installState)
        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            0 -> InstalledPluginsTab(
                plugins = plugins,
                onToggle = { plugin -> vm.togglePlugin(plugin.manifest.id, !plugin.enabled) },
                onRemove = { plugin -> vm.removePlugin(plugin.manifest.id) },
            )
            1 -> RegistryTab(
                entries = vm.registryPlugins,
                installedIds = plugins.map { it.manifest.id }.toSet(),
                onInstall = vm::installFromRegistry,
            )
        }
    }

    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text("Install Plugin") },
            text = {
                Column {
                    Text("Enter the plugin manifest URL (.json):")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        singleLine = true,
                        label = { Text("Manifest URL") },
                        placeholder = { Text("https://plugins.litebox.tv/tmdb/plugin.json") },
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.installPlugin(urlInput)
                    showInstallDialog = false
                    urlInput = ""
                }) { Text("Install") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showInstallDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun InstallStateBanner(state: InstallState) {
    when (state) {
        InstallState.Idle -> Unit
        InstallState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        is InstallState.Error -> Text(
            text = state.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        is InstallState.Success -> Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "Installed ${state.plugin.manifest.name}!",
                color = Color(0xFF34A853),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun InstalledPluginsTab(
    plugins: List<InstalledPlugin>,
    onToggle: (InstalledPlugin) -> Unit,
    onRemove: (InstalledPlugin) -> Unit,
) {
    if (plugins.isEmpty()) {
        EmptyState("No plugins installed. Browse featured plugins or install one by URL.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(plugins) { plugin ->
                PluginRow(
                    plugin = plugin,
                    onToggle = { onToggle(plugin) },
                    onRemove = { onRemove(plugin) },
                )
            }
        }
    }
}

@Composable
private fun RegistryTab(
    entries: List<RegistryEntry>,
    installedIds: Set<String>,
    onInstall: (RegistryEntry) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(entries) { entry ->
            RegistryRow(
                entry = entry,
                installed = entry.id in installedIds,
                onInstall = { onInstall(entry) },
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun RegistryRow(
    entry: RegistryEntry,
    installed: Boolean,
    onInstall: () -> Unit,
) {
    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.name, style = MaterialTheme.typography.titleMedium)
                    TypeBadge(entry.type)
                }
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = "by ${entry.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Button(onClick = onInstall, enabled = !installed) {
                Text(if (installed) "Installed" else "Install")
            }
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    Surface(shape = MaterialTheme.shapes.small) {
        Text(
            text = type,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(plugin.manifest.name, style = MaterialTheme.typography.titleMedium)
                    TypeBadge(plugin.manifest.type.name)
                }
                Text(
                    text = "${plugin.manifest.version} · ${plugin.manifest.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = plugin.manifest.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
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

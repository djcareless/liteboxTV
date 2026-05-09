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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import tv.litebox.domain.model.InstalledPlugin
import tv.litebox.domain.model.PluginSetting
import tv.litebox.domain.model.SettingType

@Composable
fun PluginDetailScreen(
    pluginId: String,
    onBack: () -> Unit,
    vm: PluginDetailViewModel = viewModel(factory = PluginDetailViewModelFactory(pluginId)),
) {
    val uiState by vm.uiState.collectAsState()
    val plugin = uiState.plugin

    if (plugin == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Plugin not found",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plugin.manifest.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "v${plugin.manifest.version} · by ${plugin.manifest.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (plugin.manifest.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = plugin.manifest.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Enable/Disable toggle
        Card(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (plugin.enabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = plugin.enabled,
                    onCheckedChange = { vm.toggleEnabled() },
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Settings section
        if (plugin.manifest.settingsSchema.isNotEmpty()) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(plugin.manifest.settingsSchema.size) { index ->
                    val setting = plugin.manifest.settingsSchema[index]
                    val currentValue = uiState.editingSettings[setting.key]
                        ?: setting.defaultValue
                        ?: ""

                    SettingField(
                        setting = setting,
                        value = currentValue,
                        onValueChange = { vm.updateSetting(setting.key, it) },
                    )
                }

                // Save button row
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { vm.saveSettings() },
                            enabled = !uiState.isSaving,
                        ) {
                            Text(if (uiState.isSaving) "Saving…" else "Save Settings")
                        }
                        if (uiState.saveSuccess) {
                            Text(
                                "✓ Saved",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        val err = uiState.error
                        if (err != null) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "This plugin has no configurable settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
        }

        Spacer(Modifier.height(16.dp))

        // Uninstall button at bottom
        OutlinedButton(
            onClick = { vm.showUninstallConfirm() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Uninstall Plugin", color = MaterialTheme.colorScheme.error)
        }
    }

    // Uninstall confirmation dialog
    if (uiState.showUninstallConfirm) {
        AlertDialog(
            onDismissRequest = { vm.dismissUninstallConfirm() },
            title = { Text("Uninstall Plugin") },
            text = {
                Text("Are you sure you want to uninstall ${plugin.manifest.name}? This will remove all plugin data and settings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.dismissUninstallConfirm()
                        vm.uninstall()
                        onBack()
                    },
                ) {
                    Text("Uninstall")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { vm.dismissUninstallConfirm() }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingField(
    setting: PluginSetting,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (setting.type) {
            SettingType.PASSWORD -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(setting.label) },
                    placeholder = { setting.hint?.let { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SettingType.BOOLEAN -> {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = setting.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Switch(
                            checked = value.toBooleanStrictOrNull() ?: false,
                            onCheckedChange = { onValueChange(it.toString()) },
                        )
                    }
                }
            }
            SettingType.NUMBER -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(setting.label) },
                    placeholder = { setting.hint?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                // TEXT, SELECT — plain text field
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(setting.label) },
                    placeholder = { setting.hint?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

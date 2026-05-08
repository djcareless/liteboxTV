package tv.litebox.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.litebox.ui.components.MediaCard

@Composable
fun LibraryScreen(
    sourceType: String,
    onNavigateToPlayer: (String) -> Unit,
    onBack: () -> Unit,
    vm: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory(sourceType)),
) {
    val items by vm.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
    ) {
        Text(
            text = sourceType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items) { item ->
                MediaCard(
                    item = item,
                    onClick = { onNavigateToPlayer(item.id) },
                )
            }
        }
    }
}

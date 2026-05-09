package tv.litebox.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.litebox.ui.components.MediaCard
import tv.litebox.ui.components.NavSidebar

/**
 * Home screen — main hub with:
 * - Left sidebar navigation
 * - "Continue Watching" row
 * - "Movies" row
 * - "TV Shows" row
 * - "Recently Added" row
 */
@Composable
fun HomeScreen(
    onNavigateToLibrary: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToSettings: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Left sidebar
        NavSidebar(
            onHome = { /* already home */ },
            onMovies = { onNavigateToLibrary("MOVIE") },
            onTVShows = { onNavigateToLibrary("TV_SHOW") },
            onPlugins = onNavigateToPlugins,
            onSettings = onNavigateToSettings,
            modifier = Modifier
                .fillMaxHeight()
                .width(72.dp),
        )

        // Content area
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Greeting / header
            item {
                Text(
                    text = "LiteBox",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // Empty state — no media yet
            if (!uiState.isLoading && uiState.continueWatching.isEmpty() && uiState.recentMovies.isEmpty() && uiState.recentTvShows.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📂",
                                style = MaterialTheme.typography.displayLarge,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No media yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Go to Plugins to add a media source,\nor navigate to Settings to scan local files.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // Continue Watching
            if (uiState.continueWatching.isNotEmpty()) {
                item {
                    Text(
                        text = "Continue Watching",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.continueWatching) { item ->
                            MediaCard(
                                item = item,
                                onClick = { onNavigateToPlayer(item.id) },
                                showProgress = true,
                            )
                        }
                    }
                }
            }

            // Recent Movies
            if (uiState.recentMovies.isNotEmpty()) {
                item {
                    Text(
                        text = "Movies",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.recentMovies) { item ->
                            MediaCard(
                                item = item,
                                onClick = { onNavigateToPlayer(item.id) },
                            )
                        }
                    }
                }
            }

            // TV Shows
            if (uiState.recentTvShows.isNotEmpty()) {
                item {
                    Text(
                        text = "TV Shows",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.recentTvShows) { item ->
                            MediaCard(
                                item = item,
                                onClick = { onNavigateToPlayer(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

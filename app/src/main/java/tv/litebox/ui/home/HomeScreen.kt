package tv.litebox.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
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
        TvLazyColumn(
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

            // Continue Watching
            if (uiState.continueWatching.isNotEmpty()) {
                item {
                    Text(
                        text = "Continue Watching",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    TvLazyRow(
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
                    TvLazyRow(
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
                    TvLazyRow(
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

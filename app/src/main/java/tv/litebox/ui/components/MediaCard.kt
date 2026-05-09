package tv.litebox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import tv.litebox.domain.model.MediaItem

/**
 * MediaCard — displays a media item as a poster card.
 * Shows scraper-enriched metadata (poster, rating, overview) when available.
 * Sized for 5-column TV grid (approx 200x300dp).
 */
@Composable
fun MediaCard(
    item: MediaItem,
    onClick: () -> Unit,
    showProgress: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(180.dp)
            .height(270.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Poster image: prefer scraper posterUrl, fallback to thumbnailUrl ──
            val imageModel = item.posterUrl ?: item.thumbnailUrl
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // Placeholder when no image is available
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.title.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Rating badge (top-end) ──────────────────────────────────────
            if (item.rating != null && item.rating > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xE6FF8F00))  // amber accent
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = String.format("%.1f", item.rating),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color.Black,
                        ),
                    )
                }
            }

            // ── Bottom overlay with title + metadata ───────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xDD000000),
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Year
                    if (item.year != null) {
                        Text(
                            text = item.year.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }

                    // Overview snippet (from scraper)
                    val overview = item.overview ?: item.description
                    if (!overview.isNullOrBlank()) {
                        Text(
                            text = overview.take(80) + if (overview.length > 80) "…" else "",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Genre chips (condensed)
                    if (item.genres.isNotEmpty()) {
                        Text(
                            text = item.genres.take(3).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = Color.White.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Resume progress bar
                    if (showProgress && item.duration != null && item.resumePosition > 0) {
                        val progress = (item.resumePosition.toFloat() / item.duration).coerceIn(0f, 1f)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                        )
                    }
                }
            }
        }
    }
}

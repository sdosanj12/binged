package ca.bcit.binged.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.bcit.binged.data.model.Episode
import ca.bcit.binged.data.model.Season
import ca.bcit.binged.ui.components.ChoiceChipRow
import coil3.compose.AsyncImage

// The "Episodes" section of the details screen: season chips + episode list.
// If onEpisodeClick is set (the show is marked Watching), tapping an episode
// marks it as where the user is up to.
@Composable
fun EpisodeSection(
    seasons: List<Season>,
    selectedSeason: Int?,
    episodesState: EpisodesState,
    currentSeason: Int?,
    currentEpisode: Int?,
    onSeasonSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onEpisodeClick: ((Episode) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Episodes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (seasons.isEmpty()) {
            Text(
                "No episode information available.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            ChoiceChipRow(
                options = seasons,
                selected = seasons.firstOrNull { it.number == selectedSeason },
                onSelect = { onSeasonSelected(it.number) },
                label = { it.name }
            )

            if (onEpisodeClick != null) {
                Text(
                    "Tap an episode to mark where you're up to.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (episodesState) {
                    is EpisodesState.Loading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    is EpisodesState.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Couldn't load episodes.",
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRetry) { Text("Retry") }
                    }
                    is EpisodesState.Success -> episodesState.episodes.forEach { episode ->
                        val isCurrent = selectedSeason == currentSeason && episode.number == currentEpisode
                        EpisodeRow(
                            episode = episode,
                            isCurrent = isCurrent,
                            onClick = onEpisodeClick?.let { click -> { click(episode) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, isCurrent: Boolean, onClick: (() -> Unit)?) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = MaterialTheme.shapes.medium,
        color = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (episode.stillUrl != null) {
                AsyncImage(
                    model = episode.stillUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(112.dp)
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.small)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${episode.number}. ${episode.name}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val info = listOfNotNull(
                    episode.airDate.ifBlank { null },
                    episode.runtimeMinutes?.let { "$it min" }
                ).joinToString(" · ")
                if (info.isNotEmpty()) {
                    Text(
                        info,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isCurrent) {
                    Text(
                        "You're up to here",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (episode.overview.isNotBlank()) {
                    Text(
                        episode.overview,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
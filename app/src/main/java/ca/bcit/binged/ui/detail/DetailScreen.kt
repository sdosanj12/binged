package ca.bcit.binged.ui.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.bcit.binged.data.local.WatchlistEntity
import ca.bcit.binged.data.model.Episode
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.ShowDetails
import ca.bcit.binged.data.model.WatchStatus
import ca.bcit.binged.ui.AppViewModelProvider
import ca.bcit.binged.ui.components.ChoiceChipRow
import ca.bcit.binged.ui.components.LoadingView
import ca.bcit.binged.ui.components.MessageView
import ca.bcit.binged.ui.components.PosterImage
import ca.bcit.binged.ui.components.ScreenHeader

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onWriteReview: (MediaType, Int) -> Unit,
    viewModel: DetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val details = uiState.details

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = details?.name.orEmpty(), onBack = onBack)

        when {
            uiState.isLoading -> LoadingView()
            uiState.errorMessage != null -> MessageView(
                message = "Couldn't load this title.\n${uiState.errorMessage}",
                actionLabel = "Retry",
                onAction = viewModel::loadDetails
            )
            details != null -> DetailContent(
                details = details,
                entry = uiState.watchlistEntry,
                hasReview = uiState.hasReview,
                selectedSeason = uiState.selectedSeason,
                episodesState = uiState.episodes,
                onStatusSelected = viewModel::setStatus,
                onSeasonChange = viewModel::changeSeason,
                onEpisodeChange = viewModel::changeEpisode,
                onRemove = viewModel::removeFromList,
                onWriteReview = { onWriteReview(details.mediaType, details.id) },
                onSeasonSelected = viewModel::selectSeason,
                onRetryEpisodes = viewModel::retryEpisodes,
                onEpisodeClick = { season, episode -> viewModel.setProgress(season, episode) }
            )
        }
    }
}

@Composable
private fun DetailContent(
    details: ShowDetails,
    entry: WatchlistEntity?,
    hasReview: Boolean,
    selectedSeason: Int?,
    episodesState: EpisodesState,
    onStatusSelected: (WatchStatus) -> Unit,
    onSeasonChange: (Int) -> Unit,
    onEpisodeChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onWriteReview: () -> Unit,
    onSeasonSelected: (Int) -> Unit,
    onRetryEpisodes: () -> Unit,
    onEpisodeClick: (season: Int, episode: Int) -> Unit
) {
    val isTv = details.mediaType == MediaType.TV

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Summary(details, modifier = Modifier.padding(horizontal = 16.dp))

        if (details.genres.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                details.genres.forEach { GenreTag(it) }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Overview", style = MaterialTheme.typography.titleMedium)
            Text(
                text = details.overview.ifBlank { "No overview available." },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // ----- My List -----
        Text(
            "My List",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        ChoiceChipRow(
            options = WatchStatus.optionsFor(details.mediaType),
            selected = entry?.status,
            onSelect = onStatusSelected,
            label = { it.labelFor(details.mediaType) }
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (entry == null) {
                Text(
                    "Pick a status to add this to My List.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                when (entry.status) {
                    WatchStatus.WATCHING -> {
                        ProgressStepper("Season", entry.currentSeason, onSeasonChange)
                        ProgressStepper("Episode", entry.currentEpisode, onEpisodeChange)
                    }
                    WatchStatus.FINISHED -> Button(
                        onClick = onWriteReview,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (hasReview) "Edit my review" else "Write a review")
                    }
                    WatchStatus.WANT_TO_WATCH -> Unit
                }
                TextButton(onClick = onRemove) { Text("Remove from My List") }
            }
        }

        // ----- Episodes (TV only) -----
        if (isTv) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            val isWatching = entry?.status == WatchStatus.WATCHING
            EpisodeSection(
                seasons = details.seasons,
                selectedSeason = selectedSeason,
                episodesState = episodesState,
                currentSeason = if (isWatching) entry?.currentSeason else null,
                currentEpisode = if (isWatching) entry?.currentEpisode else null,
                onSeasonSelected = onSeasonSelected,
                onRetry = onRetryEpisodes,
                onEpisodeClick = if (isWatching && selectedSeason != null) {
                    { episode: Episode -> onEpisodeClick(selectedSeason, episode.number) }
                } else {
                    null
                }
            )
        }
    }
}

@Composable
private fun Summary(details: ShowDetails, modifier: Modifier = Modifier) {
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    val year = details.releaseDate.take(4)

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        PosterImage(
            url = details.posterUrl,
            contentDescription = details.name,
            modifier = Modifier
                .width(120.dp)
                .clip(MaterialTheme.shapes.medium)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(details.name, style = MaterialTheme.typography.titleLarge)
            Text(
                listOf(details.mediaType.label, year).filter { it.isNotBlank() }.joinToString(" · "),
                color = secondary
            )
            Text("★ %.1f / 10 on TMDB".format(details.rating), color = secondary)

            when (details.mediaType) {
                MediaType.TV -> if (details.numberOfSeasons > 0) {
                    Text(
                        "${plural(details.numberOfSeasons, "season")} · ${plural(details.numberOfEpisodes, "episode")}",
                        color = secondary
                    )
                }
                MediaType.MOVIE -> details.runtimeMinutes?.let {
                    Text(formatRuntime(it), color = secondary)
                }
            }

            if (details.status.isNotBlank()) {
                Text(details.status, color = secondary)
            }
        }
    }
}

@Composable
private fun GenreTag(name: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// "Season  [−] 2 [+]": used for both season and episode.
@Composable
private fun ProgressStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onChange(-1) }, enabled = value > 1) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        FilledTonalIconButton(onClick = { onChange(1) }) {
            Icon(Icons.Default.Add, contentDescription = "Next $label")
        }
    }
}

private fun plural(count: Int, word: String) = if (count == 1) "1 $word" else "$count ${word}s"

// 135 -> "2h 15m"
private fun formatRuntime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours == 0 -> "${mins}m"
        mins == 0 -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}
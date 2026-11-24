package ca.bcit.binged.ui.mylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.bcit.binged.data.local.WatchlistEntity
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.WatchStatus
import ca.bcit.binged.ui.AppViewModelProvider
import ca.bcit.binged.ui.components.ChoiceChipRow
import ca.bcit.binged.ui.components.LoadingView
import ca.bcit.binged.ui.components.MessageView
import ca.bcit.binged.ui.components.PosterImage
import ca.bcit.binged.ui.components.ScreenHeader

@Composable
fun MyListScreen(
    onShowClick: (MediaType, Int) -> Unit,
    viewModel: MyListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "My List")

        // null stands for "All".
        ChoiceChipRow(
            options = listOf<WatchStatus?>(null) + WatchStatus.entries,
            selected = uiState.filter,
            onSelect = viewModel::setFilter,
            label = { it?.label ?: "All" }
        )

        when {
            uiState.isLoading -> LoadingView()
            uiState.totalCount == 0 -> MessageView(
                "Your list is empty.\nOpen a show or movie from Trending or Search and pick a status to add it."
            )
            uiState.entries.isEmpty() -> MessageView(
                "No shows marked \"${uiState.filter?.label}\" yet."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // A movie and a show can share an id, so the key includes the type.
                items(uiState.entries, key = { "${it.mediaType}-${it.showId}" }) { entry ->
                    WatchlistRow(entry = entry, onClick = { onShowClick(entry.mediaType, entry.showId) })
                }
            }
        }
    }
}

@Composable
private fun WatchlistRow(entry: WatchlistEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PosterImage(
                url = entry.posterUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(56.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // e.g. "TV · Watching · S2 E5" or "Movie · Watched"
                val statusText = when (entry.status) {
                    WatchStatus.WATCHING -> "Watching · S${entry.currentSeason} E${entry.currentEpisode}"
                    else -> entry.status.labelFor(entry.mediaType)
                }
                Text(
                    text = "${entry.mediaType.label} · $statusText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
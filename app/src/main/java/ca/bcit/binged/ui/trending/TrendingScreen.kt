package ca.bcit.binged.ui.trending

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.bcit.binged.data.model.Genre
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.ui.AppViewModelProvider
import ca.bcit.binged.ui.components.ChoiceChipRow
import ca.bcit.binged.ui.components.LoadingView
import ca.bcit.binged.ui.components.MessageView
import ca.bcit.binged.ui.components.ScreenHeader
import ca.bcit.binged.ui.components.ShowGrid

@Composable
fun TrendingScreen(
    onShowClick: (MediaType, Int) -> Unit,
    viewModel: TrendingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Trending this week")

        when (val state = uiState) {
            is TrendingUiState.Loading -> LoadingView()
            is TrendingUiState.Error -> MessageView(
                message = "Couldn't load trending.\n${state.message}",
                actionLabel = "Retry",
                onAction = viewModel::loadTrending
            )
            is TrendingUiState.Success -> {
                Column(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // null stands for "All" in both rows.
                    ChoiceChipRow(
                        options = listOf<MediaType?>(null) + MediaType.entries,
                        selected = state.selectedType,
                        onSelect = viewModel::selectType,
                        label = { it?.pluralLabel ?: "All" }
                    )
                    ChoiceChipRow(
                        options = listOf<Genre?>(null) + state.genres,
                        selected = state.selectedGenre,
                        onSelect = viewModel::selectGenre,
                        label = { it?.name ?: "All genres" }
                    )
                }
                ShowGrid(shows = state.shows, onShowClick = onShowClick)
            }
        }
    }
}
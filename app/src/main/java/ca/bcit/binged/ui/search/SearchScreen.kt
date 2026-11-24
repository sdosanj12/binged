package ca.bcit.binged.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.ui.AppViewModelProvider
import ca.bcit.binged.ui.components.ChoiceChipRow
import ca.bcit.binged.ui.components.LoadingView
import ca.bcit.binged.ui.components.MessageView
import ca.bcit.binged.ui.components.ScreenHeader
import ca.bcit.binged.ui.components.ShowGrid

@Composable
fun SearchScreen(
    onShowClick: (MediaType, Int) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val query by viewModel.query.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Search")

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Search TV shows and movies") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // null stands for "All".
        ChoiceChipRow(
            options = listOf<MediaType?>(null) + MediaType.entries,
            selected = selectedType,
            onSelect = viewModel::selectType,
            label = { it?.pluralLabel ?: "All" },
            modifier = Modifier.padding(top = 8.dp)
        )

        when (val state = uiState) {
            is SearchUiState.Idle -> MessageView("Search for a TV show or movie to see its details and add it to your list.")
            is SearchUiState.Loading -> LoadingView()
            is SearchUiState.Error -> MessageView(
                message = "Search failed.\n${state.message}",
                actionLabel = "Retry",
                onAction = viewModel::retry
            )
            is SearchUiState.Success ->
                if (state.shows.isEmpty()) {
                    MessageView("No results for \"$query\".")
                } else {
                    ShowGrid(shows = state.shows, onShowClick = onShowClick)
                }
        }
    }
}
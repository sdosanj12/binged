package ca.bcit.binged.ui.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.bcit.binged.ui.AppViewModelProvider
import ca.bcit.binged.ui.components.LoadingView
import ca.bcit.binged.ui.components.PosterImage
import ca.bcit.binged.ui.components.RatingBar
import ca.bcit.binged.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun ReviewEditorScreen(
    onDone: () -> Unit,
    viewModel: ReviewEditorViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = if (uiState.isExistingReview) "Edit review" else "Write a review",
            onBack = onDone
        )

        if (uiState.isLoading) {
            LoadingView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PosterImage(
                        url = uiState.posterUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .width(72.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Text(uiState.showName, style = MaterialTheme.typography.titleLarge)
                }

                Text("Your rating", style = MaterialTheme.typography.titleMedium)
                RatingBar(
                    rating = uiState.rating,
                    starSize = 36.dp,
                    onRatingChange = viewModel::onRatingChange
                )

                OutlinedTextField(
                    value = uiState.reviewText,
                    onValueChange = viewModel::onReviewTextChange,
                    label = { Text("Your review (optional)") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Would you rewatch it?",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.wouldRewatch,
                        onCheckedChange = viewModel::onWouldRewatchChange
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveReview()
                            onDone()
                        }
                    },
                    enabled = uiState.canSave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save review")
                }
                if (!uiState.canSave) {
                    Text(
                        "Tap a star to give a rating before saving.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (uiState.isExistingReview) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete review")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this review?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    coroutineScope.launch {
                        viewModel.deleteReview()
                        onDone()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
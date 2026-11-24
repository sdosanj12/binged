package ca.bcit.binged.ui.reviews

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.bcit.binged.data.local.ReviewEntity
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.ui.AppViewModelProvider
import ca.bcit.binged.ui.components.ChoiceChipRow
import ca.bcit.binged.ui.components.LoadingView
import ca.bcit.binged.ui.components.MessageView
import ca.bcit.binged.ui.components.PosterImage
import ca.bcit.binged.ui.components.RatingBar
import ca.bcit.binged.ui.components.ScreenHeader

@Composable
fun ReviewsScreen(
    onReviewClick: (MediaType, Int) -> Unit,
    viewModel: ReviewsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "My Reviews")

        ChoiceChipRow(
            options = ReviewSort.entries,
            selected = uiState.sort,
            onSelect = viewModel::setSort,
            label = { it.label }
        )

        when {
            uiState.isLoading -> LoadingView()
            uiState.reviews.isEmpty() -> MessageView(
                "No reviews yet.\nMark a show as Finished (or a movie as Watched) on its details page to write one."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // A movie and a show can share an id, so the key includes the type.
                items(uiState.reviews, key = { "${it.mediaType}-${it.showId}" }) { review ->
                    ReviewCard(review = review, onClick = { onReviewClick(review.mediaType, review.showId) })
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PosterImage(
                url = review.posterUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(64.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(review.showName, style = MaterialTheme.typography.titleMedium)
                Text(
                    review.mediaType.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RatingBar(rating = review.rating)
                if (review.reviewText.isNotBlank()) {
                    Text(
                        review.reviewText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (review.wouldRewatch) {
                    Text(
                        "Would rewatch",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
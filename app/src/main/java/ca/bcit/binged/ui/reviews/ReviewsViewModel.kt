package ca.bcit.binged.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bcit.binged.data.local.ReviewEntity
import ca.bcit.binged.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ReviewSort(val label: String) {
    NEWEST("Newest"),
    HIGHEST_RATED("Highest rated")
}

data class ReviewsUiState(
    val isLoading: Boolean = true,
    val reviews: List<ReviewEntity> = emptyList(),
    val sort: ReviewSort = ReviewSort.NEWEST
)

class ReviewsViewModel(repository: ReviewRepository) : ViewModel() {

    private val sort = MutableStateFlow(ReviewSort.NEWEST)

    val uiState: StateFlow<ReviewsUiState> =
        combine(repository.getAll(), sort) { reviews, selected ->
            ReviewsUiState(
                isLoading = false,
                reviews = when (selected) {
                    ReviewSort.NEWEST -> reviews.sortedByDescending { it.updatedAt }
                    ReviewSort.HIGHEST_RATED -> reviews.sortedWith(
                        compareByDescending<ReviewEntity> { it.rating }.thenByDescending { it.updatedAt }
                    )
                },
                sort = selected
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReviewsUiState()
        )

    fun setSort(newSort: ReviewSort) {
        sort.value = newSort
    }
}
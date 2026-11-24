package ca.bcit.binged.ui.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bcit.binged.data.local.ReviewEntity
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.repository.ReviewRepository
import ca.bcit.binged.data.repository.WatchlistRepository
import ca.bcit.binged.ui.navigation.MEDIA_TYPE_ARG
import ca.bcit.binged.ui.navigation.SHOW_ID_ARG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val id: Int = checkNotNull(savedStateHandle.get<Int>(SHOW_ID_ARG))
    private val type: MediaType =
        MediaType.valueOf(checkNotNull(savedStateHandle.get<String>(MEDIA_TYPE_ARG)))

    private val _uiState = MutableStateFlow(ReviewEditorUiState())
    val uiState: StateFlow<ReviewEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = reviewRepository.getReview(id, type).first()
            _uiState.value = if (existing != null) {
                // Editing: fill the form with the saved review.
                ReviewEditorUiState(
                    isLoading = false,
                    showName = existing.showName,
                    posterUrl = existing.posterUrl,
                    rating = existing.rating,
                    reviewText = existing.reviewText,
                    wouldRewatch = existing.wouldRewatch,
                    isExistingReview = true
                )
            } else {
                // New review: take the name and poster from My List.
                val entry = watchlistRepository.getEntry(id, type).first()
                ReviewEditorUiState(
                    isLoading = false,
                    showName = entry?.name.orEmpty(),
                    posterUrl = entry?.posterUrl
                )
            }
        }
    }

    fun onRatingChange(rating: Int) = _uiState.update { it.copy(rating = rating) }

    fun onReviewTextChange(text: String) = _uiState.update { it.copy(reviewText = text) }

    fun onWouldRewatchChange(value: Boolean) = _uiState.update { it.copy(wouldRewatch = value) }

    suspend fun saveReview() {
        val state = _uiState.value
        if (!state.canSave) return
        reviewRepository.save(
            ReviewEntity(
                showId = id,
                mediaType = type,
                showName = state.showName,
                posterUrl = state.posterUrl,
                rating = state.rating,
                reviewText = state.reviewText.trim(),
                wouldRewatch = state.wouldRewatch
            )
        )
    }

    suspend fun deleteReview() {
        reviewRepository.delete(id, type)
    }
}
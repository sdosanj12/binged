package ca.bcit.binged.ui.reviews

data class ReviewEditorUiState(
    val isLoading: Boolean = true,
    val showName: String = "",
    val posterUrl: String? = null,
    val rating: Int = 0,
    val reviewText: String = "",
    val wouldRewatch: Boolean = false,
    val isExistingReview: Boolean = false
) {
    val canSave: Boolean get() = rating > 0
}
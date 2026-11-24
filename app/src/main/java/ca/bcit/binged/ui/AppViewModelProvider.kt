package ca.bcit.binged.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.bcit.binged.BingedApplication
import ca.bcit.binged.ui.detail.DetailViewModel
import ca.bcit.binged.ui.mylist.MyListViewModel
import ca.bcit.binged.ui.reviews.ReviewEditorViewModel
import ca.bcit.binged.ui.reviews.ReviewsViewModel
import ca.bcit.binged.ui.search.SearchViewModel
import ca.bcit.binged.ui.trending.TrendingViewModel

// Tells Compose how to build each ViewModel with the repositories it needs.
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            TrendingViewModel(bingedApplication().container.showRepository)
        }
        initializer {
            SearchViewModel(bingedApplication().container.showRepository)
        }
        initializer {
            val container = bingedApplication().container
            DetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                showRepository = container.showRepository,
                watchlistRepository = container.watchlistRepository,
                reviewRepository = container.reviewRepository
            )
        }
        initializer {
            MyListViewModel(bingedApplication().container.watchlistRepository)
        }
        initializer {
            ReviewsViewModel(bingedApplication().container.reviewRepository)
        }
        initializer {
            val container = bingedApplication().container
            ReviewEditorViewModel(
                savedStateHandle = createSavedStateHandle(),
                reviewRepository = container.reviewRepository,
                watchlistRepository = container.watchlistRepository
            )
        }
    }
}

private fun CreationExtras.bingedApplication(): BingedApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BingedApplication
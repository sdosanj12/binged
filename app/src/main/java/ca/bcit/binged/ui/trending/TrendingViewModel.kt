package ca.bcit.binged.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bcit.binged.data.model.Genre
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.Show
import ca.bcit.binged.data.repository.ShowRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TrendingUiState {
    data object Loading : TrendingUiState
    data class Error(val message: String) : TrendingUiState
    data class Success(
        val shows: List<Show>,          // after both filters
        val selectedType: MediaType?,   // null = TV and movies
        val genres: List<Genre>,        // only genres that appear in the results
        val selectedGenre: Genre?       // null = all genres
    ) : TrendingUiState
}

class TrendingViewModel(private val repository: ShowRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TrendingUiState>(TrendingUiState.Loading)
    val uiState: StateFlow<TrendingUiState> = _uiState.asStateFlow()

    private var allShows: List<Show> = emptyList()
    private var allGenres: List<Genre> = emptyList()
    private var selectedType: MediaType? = null
    private var selectedGenre: Genre? = null

    init {
        loadTrending()
    }

    fun loadTrending() {
        _uiState.value = TrendingUiState.Loading
        viewModelScope.launch {
            try {
                // Load the shows and the genre names at the same time.
                coroutineScope {
                    val showsRequest = async { repository.getTrending() }
                    val genresRequest = async { repository.getAllGenres() }
                    allShows = showsRequest.await()
                    allGenres = genresRequest.await()
                }
                selectedType = null
                selectedGenre = null
                showResults()
            } catch (e: Exception) {
                _uiState.value = TrendingUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun selectType(type: MediaType?) {
        selectedType = type
        showResults()
    }

    fun selectGenre(genre: Genre?) {
        selectedGenre = genre
        showResults()
    }

    private fun showResults() {
        val type = selectedType
        val ofType = if (type == null) allShows else allShows.filter { it.mediaType == type }

        // Only offer genres that at least one result has.
        val availableGenres = allGenres.filter { genre -> ofType.any { genre.id in it.genreIds } }

        // If the selected genre doesn't exist for this type (e.g. "Kids" for movies), clear it.
        val genre = selectedGenre?.takeIf { it in availableGenres }
        selectedGenre = genre

        _uiState.value = TrendingUiState.Success(
            shows = if (genre == null) ofType else ofType.filter { genre.id in it.genreIds },
            selectedType = type,
            genres = availableGenres,
            selectedGenre = genre
        )
    }
}
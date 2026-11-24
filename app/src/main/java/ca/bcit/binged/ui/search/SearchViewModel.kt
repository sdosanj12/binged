package ca.bcit.binged.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.Show
import ca.bcit.binged.data.repository.ShowRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val shows: List<Show>) : SearchUiState  // after the type filter
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(private val repository: ShowRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedType = MutableStateFlow<MediaType?>(null) // null = TV and movies
    val selectedType: StateFlow<MediaType?> = _selectedType.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var lastResults: List<Show> = emptyList()
    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // wait until the user stops typing before calling the API
            _uiState.value = SearchUiState.Loading
            try {
                lastResults = repository.search(newQuery.trim())
                showResults()
            } catch (e: CancellationException) {
                throw e // a newer search replaced this one
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    // Filtering doesn't need a new API call: it just filters the last results.
    fun selectType(type: MediaType?) {
        _selectedType.value = type
        if (_uiState.value is SearchUiState.Success) showResults()
    }

    fun retry() = onQueryChange(_query.value)

    private fun showResults() {
        val type = _selectedType.value
        _uiState.value = SearchUiState.Success(
            if (type == null) lastResults else lastResults.filter { it.mediaType == type }
        )
    }
}
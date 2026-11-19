package ca.bcit.binged.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bcit.binged.data.model.Show
import ca.bcit.binged.data.repository.ShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TrendingUiState {
    data object Loading : TrendingUiState
    data class Success(val shows: List<Show>) : TrendingUiState
    data class Error(val message: String) : TrendingUiState
}

class TrendingViewModel(
    private val repository: ShowRepository = ShowRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrendingUiState>(TrendingUiState.Loading)
    val uiState: StateFlow<TrendingUiState> = _uiState.asStateFlow()

    init {
        loadTrending()
    }

    fun loadTrending() {
        _uiState.value = TrendingUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                TrendingUiState.Success(repository.getTrendingShows())
            } catch (e: Exception) {
                TrendingUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}
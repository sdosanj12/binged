package ca.bcit.binged.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bcit.binged.data.local.WatchlistEntity
import ca.bcit.binged.data.model.Episode
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.ShowDetails
import ca.bcit.binged.data.model.WatchStatus
import ca.bcit.binged.data.repository.ReviewRepository
import ca.bcit.binged.data.repository.ShowRepository
import ca.bcit.binged.data.repository.WatchlistRepository
import ca.bcit.binged.ui.navigation.MEDIA_TYPE_ARG
import ca.bcit.binged.ui.navigation.SHOW_ID_ARG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EpisodesState {
    data object Loading : EpisodesState
    data class Success(val episodes: List<Episode>) : EpisodesState
    data class Error(val message: String) : EpisodesState
}

data class DetailUiState(
    val isLoading: Boolean = true,
    val details: ShowDetails? = null,
    val errorMessage: String? = null,
    val watchlistEntry: WatchlistEntity? = null, // null = not on My List
    val hasReview: Boolean = false,
    // TV only
    val selectedSeason: Int? = null,
    val episodes: EpisodesState = EpisodesState.Loading
)

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val showRepository: ShowRepository,
    private val watchlistRepository: WatchlistRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val id: Int = checkNotNull(savedStateHandle.get<Int>(SHOW_ID_ARG))
    private val type: MediaType =
        MediaType.valueOf(checkNotNull(savedStateHandle.get<String>(MEDIA_TYPE_ARG)))

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Seasons we've already downloaded, so switching back doesn't call the API again.
    private val episodeCache = mutableMapOf<Int, List<Episode>>()

    init {
        loadDetails()

        // Keep the screen in sync with the database.
        viewModelScope.launch {
            watchlistRepository.getEntry(id, type).collect { entry ->
                _uiState.update { it.copy(watchlistEntry = entry) }
            }
        }
        viewModelScope.launch {
            reviewRepository.getReview(id, type).collect { review ->
                _uiState.update { it.copy(hasReview = review != null) }
            }
        }
    }

    fun loadDetails() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val details = showRepository.getDetails(id, type)
                _uiState.update { it.copy(isLoading = false, details = details) }

                // TV: open the season the user is up to, or the first season.
                val resumeSeason = _uiState.value.watchlistEntry
                    ?.takeIf { it.status == WatchStatus.WATCHING }
                    ?.currentSeason
                val startSeason = details.seasons.firstOrNull { it.number == resumeSeason }
                    ?: details.seasons.firstOrNull()
                startSeason?.let { selectSeason(it.number) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Something went wrong")
                }
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        _uiState.update { it.copy(selectedSeason = seasonNumber) }

        val cached = episodeCache[seasonNumber]
        if (cached != null) {
            _uiState.update { it.copy(episodes = EpisodesState.Success(cached)) }
            return
        }

        _uiState.update { it.copy(episodes = EpisodesState.Loading) }
        viewModelScope.launch {
            val result = try {
                val episodes = showRepository.getSeasonEpisodes(id, seasonNumber)
                episodeCache[seasonNumber] = episodes
                EpisodesState.Success(episodes)
            } catch (e: Exception) {
                EpisodesState.Error(e.message ?: "Something went wrong")
            }
            // Only show it if the user hasn't switched to another season meanwhile.
            if (_uiState.value.selectedSeason == seasonNumber) {
                _uiState.update { it.copy(episodes = result) }
            }
        }
    }

    fun retryEpisodes() {
        _uiState.value.selectedSeason?.let { selectSeason(it) }
    }

    // Adds the item to My List, or changes its status if it's already there.
    fun setStatus(status: WatchStatus) {
        val details = _uiState.value.details ?: return
        val entry = _uiState.value.watchlistEntry
            ?.copy(status = status, updatedAt = System.currentTimeMillis())
            ?: WatchlistEntity(
                showId = details.id,
                mediaType = details.mediaType,
                name = details.name,
                posterUrl = details.posterUrl,
                status = status
            )
        save(entry)
    }

    fun changeSeason(delta: Int) {
        val entry = _uiState.value.watchlistEntry ?: return
        val maxSeason = _uiState.value.details?.numberOfSeasons?.takeIf { it > 0 } ?: Int.MAX_VALUE
        val newSeason = (entry.currentSeason + delta).coerceIn(1, maxSeason)
        if (newSeason != entry.currentSeason) {
            // Starting a new season resets the episode to 1.
            setProgress(newSeason, 1)
        }
    }

    fun changeEpisode(delta: Int) {
        val entry = _uiState.value.watchlistEntry ?: return
        val newEpisode = (entry.currentEpisode + delta).coerceAtLeast(1)
        if (newEpisode != entry.currentEpisode) {
            setProgress(entry.currentSeason, newEpisode)
        }
    }

    // Called by the steppers, and when the user taps an episode in the list.
    fun setProgress(season: Int, episode: Int) {
        val entry = _uiState.value.watchlistEntry ?: return
        save(
            entry.copy(
                currentSeason = season,
                currentEpisode = episode,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun removeFromList() {
        viewModelScope.launch { watchlistRepository.remove(id, type) }
    }

    private fun save(entry: WatchlistEntity) {
        viewModelScope.launch { watchlistRepository.save(entry) }
    }
}
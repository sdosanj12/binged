package ca.bcit.binged.data.repository

import ca.bcit.binged.data.model.Episode
import ca.bcit.binged.data.model.Genre
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.Show
import ca.bcit.binged.data.model.ShowDetails
import ca.bcit.binged.data.remote.TmdbApiService
import ca.bcit.binged.data.remote.dto.toEpisode
import ca.bcit.binged.data.remote.dto.toGenre
import ca.bcit.binged.data.remote.dto.toShowDetails
import ca.bcit.binged.data.remote.dto.toShowOrNull
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

// TV show and movie data from the TMDB API.
class ShowRepository(private val api: TmdbApiService) {

    // TMDB returns 20 results per page. Loading a few pages at once gives the
    // filters enough to work with.
    suspend fun getTrending(pages: Int = 3): List<Show> = coroutineScope {
        (1..pages)
            .map { page -> async { api.getTrending(page).results } }
            .awaitAll()
            .flatten()
            .mapNotNull { it.toShowOrNull() }
            .distinctBy { it.mediaType to it.id }
    }

    suspend fun search(query: String): List<Show> =
        api.search(query).results.mapNotNull { it.toShowOrNull() }

    // TV and movie genres combined (some, like Drama, are in both lists).
    suspend fun getAllGenres(): List<Genre> = coroutineScope {
        val tv = async { api.getTvGenres().genres }
        val movie = async { api.getMovieGenres().genres }
        (tv.await() + movie.await())
            .distinctBy { it.id }
            .map { it.toGenre() }
            .sortedBy { it.name }
    }

    suspend fun getDetails(id: Int, type: MediaType): ShowDetails = when (type) {
        MediaType.TV -> api.getTvDetails(id).toShowDetails()
        MediaType.MOVIE -> api.getMovieDetails(id).toShowDetails()
    }

    suspend fun getSeasonEpisodes(showId: Int, seasonNumber: Int): List<Episode> =
        api.getSeason(showId, seasonNumber).episodes.orEmpty().map { it.toEpisode() }
}
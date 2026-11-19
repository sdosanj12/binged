package ca.bcit.binged.data.repository

import ca.bcit.binged.data.model.Show
import ca.bcit.binged.data.remote.RetrofitInstance
import ca.bcit.binged.data.remote.TmdbApiService
import ca.bcit.binged.data.remote.dto.toShow

// The single place ViewModels get show data from.
// Later, this is also where Room (your watchlist and reviews) will plug in.
class ShowRepository(
    private val api: TmdbApiService = RetrofitInstance.api
) {
    suspend fun getTrendingShows(): List<Show> =
        api.getTrendingShows().results.map { it.toShow() }

    suspend fun searchShows(query: String): List<Show> =
        api.searchShows(query).results.map { it.toShow() }
}
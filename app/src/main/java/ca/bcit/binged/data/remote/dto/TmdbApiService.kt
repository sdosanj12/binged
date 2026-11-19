package ca.bcit.binged.data.remote

import ca.bcit.binged.data.remote.dto.TvShowListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/tv/week")
    suspend fun getTrendingShows(): TvShowListResponse

    @GET("search/tv")
    suspend fun searchShows(@Query("query") query: String): TvShowListResponse
}
package ca.bcit.binged.data.remote

import ca.bcit.binged.data.remote.dto.GenreListResponse
import ca.bcit.binged.data.remote.dto.MediaListResponse
import ca.bcit.binged.data.remote.dto.MovieDetailDto
import ca.bcit.binged.data.remote.dto.SeasonDetailDto
import ca.bcit.binged.data.remote.dto.TvShowDetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    // "all" = TV shows and movies (and people, which we filter out).
    @GET("trending/all/week")
    suspend fun getTrending(@Query("page") page: Int = 1): MediaListResponse

    // Searches TV shows and movies (and people) at once.
    @GET("search/multi")
    suspend fun search(@Query("query") query: String): MediaListResponse

    @GET("genre/tv/list")
    suspend fun getTvGenres(): GenreListResponse

    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListResponse

    @GET("tv/{id}")
    suspend fun getTvDetails(@Path("id") id: Int): TvShowDetailDto

    @GET("movie/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): MovieDetailDto

    @GET("tv/{id}/season/{season}")
    suspend fun getSeason(
        @Path("id") id: Int,
        @Path("season") seasonNumber: Int
    ): SeasonDetailDto
}
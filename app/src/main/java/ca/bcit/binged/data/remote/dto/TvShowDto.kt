package ca.bcit.binged.data.remote.dto

import ca.bcit.binged.data.model.Show
import com.google.gson.annotations.SerializedName

// Matches the JSON that TMDB sends back for a list of shows.
data class TvShowListResponse(
    val page: Int,
    val results: List<TvShowDto>
)

data class TvShowDto(
    val id: Int,
    val name: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("first_air_date") val firstAirDate: String?
)

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

fun TvShowDto.toShow() = Show(
    id = id,
    name = name,
    overview = overview,
    posterUrl = posterPath?.let { IMAGE_BASE_URL + it },
    rating = voteAverage,
    firstAirDate = firstAirDate.orEmpty()
)
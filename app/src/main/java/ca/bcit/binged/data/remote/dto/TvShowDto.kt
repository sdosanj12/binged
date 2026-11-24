package ca.bcit.binged.data.remote.dto

import ca.bcit.binged.data.model.Episode
import ca.bcit.binged.data.model.Genre
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.Season
import ca.bcit.binged.data.model.Show
import ca.bcit.binged.data.model.ShowDetails
import com.google.gson.annotations.SerializedName

// These classes match the JSON that TMDB sends back.
// Fields are nullable because TMDB sometimes leaves them out.

// ----- Lists (trending, search) -----

data class MediaListResponse(
    val page: Int,
    val results: List<MediaDto>
)

// One result from a mixed list. TV shows have "name" and "first_air_date";
// movies have "title" and "release_date".
data class MediaDto(
    val id: Int,
    @SerializedName("media_type") val mediaType: String?,
    val name: String?,
    val title: String?,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>?
)

// ----- Details -----

data class TvShowDetailDto(
    val id: Int,
    val name: String?,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int?,
    val genres: List<GenreDto>?,
    val status: String?,
    val seasons: List<SeasonSummaryDto>?
)

data class MovieDetailDto(
    val id: Int,
    val title: String?,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("release_date") val releaseDate: String?,
    val runtime: Int?,
    val genres: List<GenreDto>?,
    val status: String?
)

data class GenreListResponse(val genres: List<GenreDto>)

data class GenreDto(val id: Int, val name: String)

data class SeasonSummaryDto(
    @SerializedName("season_number") val seasonNumber: Int,
    val name: String?,
    @SerializedName("episode_count") val episodeCount: Int?
)

data class SeasonDetailDto(val episodes: List<EpisodeDto>?)

data class EpisodeDto(
    @SerializedName("episode_number") val episodeNumber: Int,
    val name: String?,
    val overview: String?,
    @SerializedName("air_date") val airDate: String?,
    val runtime: Int?,
    @SerializedName("still_path") val stillPath: String?
)

// ----- Converting to the app's own models -----

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

private fun imageUrl(path: String?): String? = path?.let { IMAGE_BASE_URL + it }

// Returns null for anything that isn't a TV show or movie (e.g. people).
fun MediaDto.toShowOrNull(): Show? {
    val type = when (mediaType) {
        "tv" -> MediaType.TV
        "movie" -> MediaType.MOVIE
        else -> return null
    }
    return Show(
        id = id,
        mediaType = type,
        name = (title ?: name).orEmpty(),
        overview = overview.orEmpty(),
        posterUrl = imageUrl(posterPath),
        rating = voteAverage ?: 0.0,
        releaseDate = (releaseDate ?: firstAirDate).orEmpty(),
        genreIds = genreIds.orEmpty()
    )
}

fun GenreDto.toGenre() = Genre(id = id, name = name)

fun TvShowDetailDto.toShowDetails() = ShowDetails(
    id = id,
    mediaType = MediaType.TV,
    name = name.orEmpty(),
    overview = overview.orEmpty(),
    posterUrl = imageUrl(posterPath),
    rating = voteAverage ?: 0.0,
    releaseDate = firstAirDate.orEmpty(),
    genres = genres.orEmpty().map { it.name },
    status = status.orEmpty(),
    numberOfSeasons = numberOfSeasons ?: 0,
    numberOfEpisodes = numberOfEpisodes ?: 0,
    // Season 0 is "Specials" on TMDB; leave it out.
    seasons = seasons.orEmpty()
        .filter { it.seasonNumber > 0 }
        .map {
            Season(
                number = it.seasonNumber,
                name = it.name ?: "Season ${it.seasonNumber}",
                episodeCount = it.episodeCount ?: 0
            )
        }
)

fun MovieDetailDto.toShowDetails() = ShowDetails(
    id = id,
    mediaType = MediaType.MOVIE,
    name = title.orEmpty(),
    overview = overview.orEmpty(),
    posterUrl = imageUrl(posterPath),
    rating = voteAverage ?: 0.0,
    releaseDate = releaseDate.orEmpty(),
    genres = genres.orEmpty().map { it.name },
    status = status.orEmpty(),
    runtimeMinutes = runtime?.takeIf { it > 0 }
)

fun EpisodeDto.toEpisode() = Episode(
    number = episodeNumber,
    name = name.orEmpty(),
    overview = overview.orEmpty(),
    airDate = airDate.orEmpty(),
    runtimeMinutes = runtime,
    stillUrl = imageUrl(stillPath)
)
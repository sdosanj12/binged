package ca.bcit.binged.data.model

// Full information about one TV show or movie, used on the details screen.
data class ShowDetails(
    val id: Int,
    val mediaType: MediaType,
    val name: String,
    val overview: String,
    val posterUrl: String?,
    val rating: Double,
    val releaseDate: String,
    val genres: List<String>,
    val status: String,
    // TV only (zero / empty for movies)
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val seasons: List<Season> = emptyList(),
    // Movies only
    val runtimeMinutes: Int? = null
)

data class Season(
    val number: Int,
    val name: String,
    val episodeCount: Int
)

data class Episode(
    val number: Int,
    val name: String,
    val overview: String,
    val airDate: String,
    val runtimeMinutes: Int?,
    val stillUrl: String?
)
package ca.bcit.binged.data.model

// A TV show or movie as it appears in a list (Trending, Search).
data class Show(
    val id: Int,
    val mediaType: MediaType,
    val name: String,
    val overview: String,
    val posterUrl: String?,
    val rating: Double,
    val releaseDate: String, // first air date for TV, release date for movies
    val genreIds: List<Int>
)
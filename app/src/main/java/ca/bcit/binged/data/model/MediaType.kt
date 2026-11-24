package ca.bcit.binged.data.model

// Whether an item is a TV show or a movie.
// TMDB numbers movies and shows separately, so the same id can mean
// two different things: we always keep the type next to the id.
enum class MediaType(val label: String, val pluralLabel: String) {
    TV("TV", "TV shows"),
    MOVIE("Movie", "Movies")
}
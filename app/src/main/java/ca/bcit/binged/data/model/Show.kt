package ca.bcit.binged.data.model

// The app's own version of a show. Screens use this, not the raw API classes.
data class Show(
    val id: Int,
    val name: String,
    val overview: String,
    val posterUrl: String?,
    val rating: Double,
    val firstAirDate: String
)
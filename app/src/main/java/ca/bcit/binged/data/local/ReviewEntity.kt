package ca.bcit.binged.data.local

import androidx.room.Entity
import ca.bcit.binged.data.model.MediaType

// One review per show or movie. Name and poster are saved too, so the
// Reviews tab works without calling the API.
@Entity(tableName = "reviews", primaryKeys = ["showId", "mediaType"])
data class ReviewEntity(
    val showId: Int,
    val mediaType: MediaType,
    val showName: String,
    val posterUrl: String?,
    val rating: Int,
    val reviewText: String,
    val wouldRewatch: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
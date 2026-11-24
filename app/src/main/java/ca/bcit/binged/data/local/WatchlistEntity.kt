package ca.bcit.binged.data.local

import androidx.room.Entity
import ca.bcit.binged.data.model.MediaType
import ca.bcit.binged.data.model.WatchStatus

// One row per show or movie on the user's list.
// The key is id + type, because a movie and a show can share the same TMDB id.
@Entity(tableName = "watchlist", primaryKeys = ["showId", "mediaType"])
data class WatchlistEntity(
    val showId: Int,
    val mediaType: MediaType,
    val name: String,
    val posterUrl: String?,
    val status: WatchStatus,
    val currentSeason: Int = 1,
    val currentEpisode: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)
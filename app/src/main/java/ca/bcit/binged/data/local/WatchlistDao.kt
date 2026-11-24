package ca.bcit.binged.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ca.bcit.binged.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE showId = :showId AND mediaType = :mediaType")
    fun getById(showId: Int, mediaType: MediaType): Flow<WatchlistEntity?>

    // Inserts a new row, or updates it if it's already on the list.
    @Upsert
    suspend fun upsert(entry: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE showId = :showId AND mediaType = :mediaType")
    suspend fun deleteById(showId: Int, mediaType: MediaType)
}
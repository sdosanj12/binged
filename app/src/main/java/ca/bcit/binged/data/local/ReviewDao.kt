package ca.bcit.binged.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ca.bcit.binged.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Query("SELECT * FROM reviews ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE showId = :showId AND mediaType = :mediaType")
    fun getById(showId: Int, mediaType: MediaType): Flow<ReviewEntity?>

    @Upsert
    suspend fun upsert(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE showId = :showId AND mediaType = :mediaType")
    suspend fun deleteById(showId: Int, mediaType: MediaType)
}
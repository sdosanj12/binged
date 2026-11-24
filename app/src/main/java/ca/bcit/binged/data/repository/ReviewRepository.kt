package ca.bcit.binged.data.repository

import ca.bcit.binged.data.local.ReviewDao
import ca.bcit.binged.data.local.ReviewEntity
import ca.bcit.binged.data.model.MediaType
import kotlinx.coroutines.flow.Flow

// The user's reviews, saved in Room.
class ReviewRepository(private val dao: ReviewDao) {

    fun getAll(): Flow<List<ReviewEntity>> = dao.getAll()

    fun getReview(id: Int, type: MediaType): Flow<ReviewEntity?> = dao.getById(id, type)

    suspend fun save(review: ReviewEntity) = dao.upsert(review)

    suspend fun delete(id: Int, type: MediaType) = dao.deleteById(id, type)
}
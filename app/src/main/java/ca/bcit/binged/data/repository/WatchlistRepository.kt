package ca.bcit.binged.data.repository

import ca.bcit.binged.data.local.WatchlistDao
import ca.bcit.binged.data.local.WatchlistEntity
import ca.bcit.binged.data.model.MediaType
import kotlinx.coroutines.flow.Flow

// The user's list, saved in Room.
class WatchlistRepository(private val dao: WatchlistDao) {

    fun getAll(): Flow<List<WatchlistEntity>> = dao.getAll()

    fun getEntry(id: Int, type: MediaType): Flow<WatchlistEntity?> = dao.getById(id, type)

    suspend fun save(entry: WatchlistEntity) = dao.upsert(entry)

    suspend fun remove(id: Int, type: MediaType) = dao.deleteById(id, type)
}
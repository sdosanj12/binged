package ca.bcit.binged.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [WatchlistEntity::class, ReviewEntity::class],
    version = 2, // version 2: added mediaType (TV / movie)
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun watchlistDao(): WatchlistDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // Only one database is ever created, even if this is called many times.
        fun getDatabase(context: Context): AppDatabase =
            Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "binged_database")
                    // While developing: if the tables change, wipe the old database
                    // instead of crashing. Bump `version` above when you change an entity.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
    }
}
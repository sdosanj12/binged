package ca.bcit.binged.data

import android.content.Context
import ca.bcit.binged.data.local.AppDatabase
import ca.bcit.binged.data.remote.RetrofitInstance
import ca.bcit.binged.data.repository.ReviewRepository
import ca.bcit.binged.data.repository.ShowRepository
import ca.bcit.binged.data.repository.WatchlistRepository

// Builds the repositories in one place so ViewModels don't create their own.
class AppContainer(context: Context) {

    private val database = AppDatabase.getDatabase(context)

    val showRepository = ShowRepository(RetrofitInstance.api)
    val watchlistRepository = WatchlistRepository(database.watchlistDao())
    val reviewRepository = ReviewRepository(database.reviewDao())
}
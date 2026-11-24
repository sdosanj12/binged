package ca.bcit.binged

import android.app.Application
import ca.bcit.binged.data.AppContainer

// Created once when the app starts. Holds the AppContainer so every
// ViewModel shares the same database and repositories.
class BingedApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
package com.myurrrjj.trackabite

import android.app.Application
import com.myurrrjj.trackabite.local.AppDatabase
import com.myurrrjj.trackabite.network.AiService
import com.myurrrjj.trackabite.repository.MealRepository
import com.myurrrjj.trackabite.repository.OfflineMealRepository

class CalorieTrackerApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
package com.example.aicalorietracker

import android.app.Application
import com.example.aicalorietracker.local.AppDatabase
import com.example.aicalorietracker.network.AiService
import com.example.aicalorietracker.repository.MealRepository
import com.example.aicalorietracker.repository.OfflineMealRepository

class CalorieTrackerApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
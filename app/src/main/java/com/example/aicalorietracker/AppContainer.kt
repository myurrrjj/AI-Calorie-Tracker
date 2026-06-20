package com.example.aicalorietracker

import android.content.Context
import com.example.aicalorietracker.health.HealthConnectManager
import com.example.aicalorietracker.local.AppDatabase
import com.example.aicalorietracker.network.AiService
import com.example.aicalorietracker.repository.DefaultHealthRepository
import com.example.aicalorietracker.repository.HealthRepository
import com.example.aicalorietracker.repository.MealRepository
import com.example.aicalorietracker.repository.OfflineMealRepository
import com.example.aicalorietracker.repository.UserPreferencesRepository

interface AppContainer {
    val mealRepository: MealRepository
    val userPreferencesRepository: UserPreferencesRepository
    val healthConnectManager: HealthConnectManager
    val healthRepository: HealthRepository

}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    private val aiService: AiService by lazy {
        AiService()
    }
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
    override val mealRepository: MealRepository by lazy {
        OfflineMealRepository(
            aiService = aiService, mealDao = database.mealDao(),
            context = context,
            savedMealDao = database.savedMealDao(),
            userPreferencesRepository = userPreferencesRepository
        )
    }
    override val healthConnectManager: HealthConnectManager by lazy{
        HealthConnectManager(context)
    }
    override val healthRepository: HealthRepository by lazy {
        DefaultHealthRepository(healthConnectManager.healthConnectClient)
    }
}
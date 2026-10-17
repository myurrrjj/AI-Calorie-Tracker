package com.myurrrjj.trackabite

import android.content.Context
import com.myurrrjj.trackabite.health.HealthConnectManager
import com.myurrrjj.trackabite.local.AppDatabase
import com.myurrrjj.trackabite.network.AiService
import com.myurrrjj.trackabite.repository.DefaultHealthRepository
import com.myurrrjj.trackabite.repository.HealthRepository
import com.myurrrjj.trackabite.repository.MealRepository
import com.myurrrjj.trackabite.repository.OfflineMealRepository
import com.myurrrjj.trackabite.repository.UserPreferencesRepository

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
            aiService = aiService,
            mealDao = database.mealDao(),
            context = context,
            savedMealDao = database.savedMealDao(),
            userPreferencesRepository = userPreferencesRepository
        )
    }
    override val healthConnectManager: HealthConnectManager by lazy {
        HealthConnectManager(context)
    }
    override val healthRepository: HealthRepository by lazy {
        DefaultHealthRepository(
            healthConnectManager.healthConnectClient,
            profileProvider = { userPreferencesRepository.profile.value })
    }
}
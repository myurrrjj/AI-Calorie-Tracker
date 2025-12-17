package com.example.aicalorietracker

import android.content.Context
import com.example.aicalorietracker.local.AppDatabase
import com.example.aicalorietracker.network.AiService
import com.example.aicalorietracker.repository.MealRepository
import com.example.aicalorietracker.repository.OfflineMealRepository

interface AppContainer{
    val mealRepository: MealRepository
}

class DefaultAppContainer(private val context : Context): AppContainer{
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    private val aiService: AiService by lazy {
        AiService()
    }
    override val mealRepository: MealRepository by lazy {
        OfflineMealRepository(aiService = aiService, mealDao = database.mealDao())
    }
}
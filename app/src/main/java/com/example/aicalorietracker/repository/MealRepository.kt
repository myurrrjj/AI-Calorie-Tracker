package com.example.aicalorietracker.repository

import com.example.aicalorietracker.local.MealDao
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.network.AiService
import kotlinx.coroutines.flow.Flow

interface MealRepository {

    fun getMealHistory(): Flow<List<MealLog>>
    suspend fun addMealLog(userText: String): Result<MealLog>
    suspend fun deleteLog(mealLog: MealLog)

    fun getMealsForDay(startTime: Long, endTime: Long): Flow<List<MealLog>>
    fun getCaloriesForDay(startTime: Long, endTime: Long): Flow<Int?>

}

class OfflineMealRepository(private val aiService: AiService, private val mealDao: MealDao) :
    MealRepository {
    override fun getMealHistory(): Flow<List<MealLog>> {
        return mealDao.getAllMeals()
    }

    override suspend fun addMealLog(userText: String): Result<MealLog> {
        val result = aiService.analyseMeal(userText)
        result.onSuccess { mealLog -> mealDao.insertMeal(mealLog) }
        return result
    }

    override suspend fun deleteLog(mealLog: MealLog) {
        mealDao.deleteMeal(mealLog)
    }

    override fun getMealsForDay(
        startTime: Long,
        endTime: Long
    ): Flow<List<MealLog>> {
        return mealDao.getMealsForDay(startTime, endTime)

    }

    override fun getCaloriesForDay(
        startTime: Long,
        endTime: Long
    ): Flow<Int?> {
        return mealDao.getTotalCaloriesForDay(startTime, endTime)
    }

}
package com.example.aicalorietracker.repository

import android.R.attr.data
import android.R.string.no
import com.example.aicalorietracker.local.MealDao
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.network.AiService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

interface MealRepository {

    fun getMealHistory(): Flow<List<MealLog>>
    suspend fun addMealLog(userText: String,date: LocalDate): Result<MealLog>
    suspend fun deleteLog(mealLog: MealLog)

    fun getMealsForDay(startTime: Long, endTime: Long): Flow<List<MealLog>>
    fun getCaloriesForDay(startTime: Long, endTime: Long): Flow<Int?>

}

class OfflineMealRepository(private val aiService: AiService, private val mealDao: MealDao) :
    MealRepository {
    override fun getMealHistory(): Flow<List<MealLog>> {
        return mealDao.getAllMeals()
    }

    override suspend fun addMealLog(userText: String,date: LocalDate): Result<MealLog> {

        val result = aiService.analyseMeal(userText)
        return result.map { mealLog ->
            val nowTime = LocalTime.now()
            val correctTimeStamp = date.atTime(nowTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val adjustedMeal = mealLog.copy(timeStamp = correctTimeStamp)
            mealDao.insertMeal(adjustedMeal)
            adjustedMeal
        }

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
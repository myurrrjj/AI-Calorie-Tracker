package com.example.aicalorietracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert
    suspend fun insertMeal(meal: MealLog)

    @Query("SELECT * FROM meal_logs WHERE timeStamp BETWEEN :startTime AND :endTime ORDER BY timeStamp ASC")
    fun getMealsForDay(startTime:Long,endTime: Long): Flow<List<MealLog>>

    @Query("SELECT SUM(calories) FROM meal_logs WHERE timeStamp BETWEEN :startTime AND :endTime")
    fun getTotalCaloriesForDay(startTime:Long,endTime: Long): Flow<Int?>


}





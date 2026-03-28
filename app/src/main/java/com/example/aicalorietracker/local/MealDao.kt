package com.example.aicalorietracker.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert
    suspend fun insertMeal(meal: MealLog)

    @Delete
    suspend fun deleteMeal(meal: MealLog)

    @Query("SELECT * FROM meal_logs ORDER BY timeStamp DESC")
    fun getAllMeals() : Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE timeStamp BETWEEN :startTime AND :endTime ORDER BY timeStamp ASC")
    fun getMealsForDay(startTime:Long,endTime: Long): Flow<List<MealLog>>

    @Query("SELECT SUM(calories) FROM meal_logs WHERE timeStamp BETWEEN :startTime AND :endTime")
    fun getTotalCaloriesForDay(startTime:Long,endTime: Long): Flow<Int?>


}

@Dao
interface SavedMealDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMeal(savedMeal : SavedMeal)

    @Delete
    suspend fun deleteSavedMeal(savedMeal: SavedMeal)

    @Query("SELECT * FROM savedMeals ORDER BY frequency DESC")
    fun getAllSavedMeals() : Flow<List<SavedMeal>>

}
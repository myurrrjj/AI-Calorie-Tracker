package com.example.aicalorietracker.repository


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.aicalorietracker.local.MealDao
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.local.SavedMeal
import com.example.aicalorietracker.local.SavedMealDao
import com.example.aicalorietracker.network.AiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

interface MealRepository {

    fun getMealHistory(): Flow<List<MealLog>>

    suspend fun addMealLog(localPath: String?, userText: String, date: LocalDate): Result<MealLog>
    suspend fun deleteLog(mealLog: MealLog)

    fun getMealsForDay(startTime: Long, endTime: Long): Flow<List<MealLog>>
    fun getCaloriesForDay(startTime: Long, endTime: Long): Flow<Int?>

    fun getSavedMeals(): Flow<List<SavedMeal>>
    suspend fun saveMealToFavourites(mealLog: MealLog)
    suspend fun deleteSavedMeal(savedMeal: SavedMeal)
    suspend fun quickLogSavedMeal(savedMeal: SavedMeal, date: LocalDate): Result<MealLog>


}

fun processAndSaveImage(context: Context, uriString: String): String? {
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = 2
        }
        val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        if (bitmap == null) return null
        val filename = "meal_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        bitmap.recycle()

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

class OfflineMealRepository(
    private val aiService: AiService,
    private val mealDao: MealDao,
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedMealDao: SavedMealDao
) : MealRepository {
    override fun getMealHistory(): Flow<List<MealLog>> {
        return mealDao.getAllMeals()
    }

    override suspend fun addMealLog(
        imageUri: String?, userText: String, date: LocalDate
    ): Result<MealLog> {


        return withContext(Dispatchers.IO) {
            try {
                val apiKey = userPreferencesRepository.getApiKey()
                    ?: throw Exception("API Key is missing. Please set it in Settings.")


                val finalLocalPath = imageUri?.let { processAndSaveImage(context, it) }
                val result = aiService.analyseMeal(apiKey, finalLocalPath, userText)

                result.map { mealLog ->
                    val nowTime = LocalTime.now()
                    val correctTimeStamp =
                        date.atTime(nowTime).atZone(ZoneId.systemDefault()).toInstant()
                            .toEpochMilli()
                    val adjustedMeal =
                        mealLog.copy(timeStamp = correctTimeStamp, imagePath = finalLocalPath)

                    mealDao.insertMeal(adjustedMeal)
                    adjustedMeal
                }
            } catch (e: Exception) {
                Result.failure(e)

            }
        }
    }

    override suspend fun deleteLog(mealLog: MealLog) {
        withContext(Dispatchers.IO) {
            mealLog.imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            mealDao.deleteMeal(mealLog)
        }
    }

    override fun getMealsForDay(
        startTime: Long, endTime: Long
    ): Flow<List<MealLog>> {
        return mealDao.getMealsForDay(startTime, endTime)

    }

    override fun getCaloriesForDay(
        startTime: Long, endTime: Long
    ): Flow<Int?> {
        return mealDao.getTotalCaloriesForDay(startTime, endTime)
    }

    override fun getSavedMeals(): Flow<List<SavedMeal>> {
        return savedMealDao.getAllSavedMeals()
    }

    override suspend fun saveMealToFavourites(mealLog: MealLog) {
        withContext(Dispatchers.IO) {
            val savedMeal = SavedMeal(
                userRequest = mealLog.userRequest,
                aiResponse = mealLog.aiResponse,
                imagePath = mealLog.imagePath,
                macros = mealLog.macros,
                micros = mealLog.micros,
                frequency = 0
            )
            savedMealDao.saveMeal(savedMeal)
        }
    }

    override suspend fun deleteSavedMeal(savedMeal: SavedMeal) {
        withContext(Dispatchers.IO) {
            savedMealDao.deleteSavedMeal(savedMeal)
        }
    }

    override suspend fun quickLogSavedMeal(
        savedMeal: SavedMeal,
        date: LocalDate
    ): Result<MealLog> {
        return withContext(Dispatchers.IO) {
            try {
                val nowTime = LocalTime.now()
                val correctTimeStamp =
                    date.atTime(nowTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val newMealLog = MealLog(
                    timeStamp = correctTimeStamp,
                    userRequest = savedMeal.userRequest,
                    aiResponse = savedMeal.aiResponse,
                    imagePath = savedMeal.imagePath,
                    macros = savedMeal.macros,
                    micros = savedMeal.micros
                )

                mealDao.insertMeal(newMealLog)
                savedMealDao.saveMeal(savedMeal.copy(frequency = savedMeal.frequency + 1))
                Result.success(newMealLog)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

}
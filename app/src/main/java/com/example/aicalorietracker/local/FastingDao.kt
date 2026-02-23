package com.example.aicalorietracker.local

import android.icu.text.MessagePattern
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingDao{
    @Insert
    suspend fun startFast(fast: FastingLog)

    @Update
    suspend fun updateFast(fast: FastingLog)

    @Query("SELECT * FROM fasting_log WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getCurrentFast(): Flow<FastingLog?>


    @Query("SELECT * FROM fasting_log WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    fun getFastHistory(): Flow<List<FastingLog>>
}
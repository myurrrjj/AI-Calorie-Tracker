package com.example.aicalorietracker.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="fasting_log")
data class FastingLog(
    @PrimaryKey
    val id : Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val targetDurationHours:Int = 16,
    val mood:String? = null
) {
    val durationMillis : Long
        get() = (endTime ?: System.currentTimeMillis())-startTime

    val progress : Float
        get(){
            val targetMillis = targetDurationHours*60*60*1000L
            val currentDuration = System.currentTimeMillis() - startTime
            return currentDuration.toFloat()/targetMillis.toFloat().coerceIn(0f,1f)

        }
}
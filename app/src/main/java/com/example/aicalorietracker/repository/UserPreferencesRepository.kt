package com.example.aicalorietracker.repository

import android.content.Context

class UserPreferencesRepository (context: Context){
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    fun getTargetCalories(): Int = prefs.getInt("target_calories",2500)
    fun updateTargetCalories(calories:Int){
        prefs.edit().putInt("target_calories",calories).apply()
    }
}
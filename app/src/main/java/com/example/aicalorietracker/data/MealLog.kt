package com.example.aicalorietracker.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timeStamp: Long = System.currentTimeMillis(),
    val userRequest: String,
    val aiResponse: String,

    @Embedded
    val macros: MacroNutrients,

    @Embedded
    val micros: MicroNutrients
)

data class MacroNutrients(
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val fiber: Int = 0,
    val sugar: Int=0
)

data class MicroNutrients(
    val vitaminA: Double = 0.0,
    val vitaminC: Double = 0.0,
    val vitaminD: Double = 0.0,
    val iron: Double = 0.0,
    val calcium: Double = 0.0,
    val sodium: Double = 0.0,
    val potassium: Double = 0.0
)
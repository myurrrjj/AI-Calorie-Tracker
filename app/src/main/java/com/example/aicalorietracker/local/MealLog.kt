package com.example.aicalorietracker.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timeStamp: Long = System.currentTimeMillis(),
    val userRequest: String,
    val aiResponse: String,
    val imagePath: String? = null,

    @Embedded
    val macros: MacroNutrients,

    @Embedded
    val micros: MicroNutrients
) {
    @Ignore
    var isAnalysing: Boolean = false
}

fun SavedMeal.toMealLog() = MealLog(
    id = 0,
    timeStamp = System.currentTimeMillis(),
    userRequest = this.userRequest,
    aiResponse = this.aiResponse,
    macros = this.macros,
    micros = this.micros,
    imagePath = this.imagePath
)

@Entity(tableName = "savedMeals")
data class SavedMeal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userRequest: String,
    val aiResponse: String,
    val imagePath: String? = null,
    val frequency : Int = 0,

    @Embedded
    val macros: MacroNutrients,

    @Embedded
    val micros: MicroNutrients
) {
    @Ignore
    var isAnalysing: Boolean = false
}

data class MacroNutrients(
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val fiber: Int = 0,
    val sugar: Int = 0
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
package com.myurrrjj.trackabite.local

import kotlinx.serialization.Serializable

@Serializable
data class AiMealResponse(
    val shortNameOfMeal: String,
    val aiResponse: String,
    val calories: Int,
    val macros: AiMacroNutrients,
    val micros: AiMicroNutrients
)

@Serializable
data class AiMacroNutrients(
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val sugar: Float
)

@Serializable
data class AiMicroNutrients(
    val vitaminA: Float,
    val vitaminC: Float,
    val vitaminD: Float,
    val iron: Float,
    val calcium: Float,
    val sodium: Float,
    val potassium: Float
)
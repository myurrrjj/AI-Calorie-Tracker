package com.example.aicalorietracker.ui.analytics

import com.example.aicalorietracker.local.MealLog

data class AnalyticsTotals(
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val fiber: Int = 0,
    val sugar: Int = 0,
    val vitaminA: Int = 0,
    val vitaminC: Int = 0,
    val vitaminD: Int = 0,
    val iron: Int = 0,
    val calcium: Int = 0,
    val sodium: Int = 0,
    val potassium: Int = 0
) {
    fun add(meal: MealLog) = AnalyticsTotals(
        protein = protein + meal.effectiveProtein,
        carbs = carbs + meal.effectiveCarbs,
        fat = fat + meal.effectiveFat,
        fiber = fiber + meal.effectiveFiber,
        sugar = sugar + meal.effectiveSugar,
        vitaminA = vitaminA + meal.effectiveVitaminA.toInt(),
        vitaminC = vitaminC + meal.effectiveVitaminC.toInt(),
        vitaminD = vitaminD + meal.effectiveVitaminD.toInt(),
        iron = iron + meal.effectiveIron.toInt(),
        calcium = calcium + meal.effectiveCalcium.toInt(),
        sodium = sodium + meal.effectiveSodium.toInt(),
        potassium = potassium + meal.effectivePotassium.toInt()
    )
}
package com.example.aicalorietracker

import com.example.aicalorietracker.local.MacroNutrients
import com.example.aicalorietracker.local.MealDao
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.local.MicroNutrients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import java.util.Calendar

fun seedDatabaseForGraphTesting(dao: MealDao) {
    GlobalScope.launch(Dispatchers.IO) {
        val calendar = Calendar.getInstance()

        for (dayOffset in 0..30) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val pastTimestamp = calendar.timeInMillis

            dao.insertMeal(
                MealLog(
                    timeStamp = pastTimestamp,
                    userRequest = "Large bowl of Dal Makhani with 2 whole wheat rotis and a side of paneer tikka",
                    aiResponse = "Analyzed successfully.",
                    quantity = 1f,
                    macros = MacroNutrients(
                        calories = 650,
                        protein = 28,
                        carbs = 60,
                        fat = 22,
                        fiber = 14,
                        sugar = 6
                    ),
                    micros = MicroNutrients(
                        vitaminA = 12.0,
                        vitaminC = 8.5,
                        vitaminD = 0.0,
                        iron = 5.2,
                        calcium = 150.0,
                        sodium = 800.0,
                        potassium = 450.0
                    )
                )
            )

//             Sample Entry 2:  meal for variation
            dao.insertMeal(
                MealLog(
                    timeStamp = pastTimestamp + 3600000, // +1 hour to separate logs on the same day
                    userRequest = "Mixed vegetable salad with chickpeas and olive oil dressing",
                    aiResponse = "Analyzed successfully.",
                    quantity = 1f,
                    macros = MacroNutrients(
                        calories = 320,
                        protein = 12,
                        carbs = 35,
                        fat = 15,
                        fiber = 10,
                        sugar = 4
                    ),
                    micros = MicroNutrients(
                        vitaminA = 45.0,
                        vitaminC = 30.0,
                        vitaminD = 0.0,
                        iron = 3.1,
                        calcium = 60.0,
                        sodium = 200.0,
                        potassium = 300.0
                    )
                )
            )
        }
    }
}
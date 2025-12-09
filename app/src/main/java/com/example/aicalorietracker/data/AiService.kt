package com.example.aicalorietracker.data

import com.example.aicalorietracker.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

class AiService {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            responseMimeType = "application/json"


        }
    )

    suspend fun analyseMeal(userText: String): Result<MealLog> {
        return try {
            val prompt = """
                You are a nutrition expert. Analyze this meal description: "$userText".
                
                Return a JSON object with the following structure:
                {
                    "aiResponse": "A friendly summary of what was logged (e.g., 'Logged 2 eggs and toast')",
                    "calories": Integer (Total calories),
                    "macros": { 
                        "protein": Integer (grams), 
                        "carbs": Integer (grams), 
                        "fat": Integer (grams), 
                        "fiber": Integer (grams), 
                        "sugar": Integer (grams) 
                    },
                    "micros": { 
                        "vitaminA": Double, 
                        "vitaminC": Double, 
                        "vitaminD": Double, 
                        "iron": Double, 
                        "calcium": Double, 
                        "sodium": Double, 
                        "potassium": Double 
                    }
                }
                
                Estimate values if exact data isn't clear. 
                If the input is not food, return 0 for all numbers and a polite message in aiResponse saying you couldn't identify the food.
            """.trimIndent()

            val response = model.generateContent(prompt)
            val jsonString = response.text ?: throw Exception("Empty response from AI")
            val resultMeal = parseJsonToMealLog(jsonString, userText)
            Result.success(resultMeal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private fun parseJsonToMealLog(jsonString: String, originalText: String): MealLog {
        val json = JSONObject(jsonString)

        val macrosJson = json.optJSONObject("macros")
        val macros = MacroNutrients(
            calories = json.optInt(
                "calories",
                0
            ),
            protein = macrosJson?.optInt("protein") ?: 0,
            carbs = macrosJson?.optInt("carbs") ?: 0,
            fat = macrosJson?.optInt("fat") ?: 0,
            fiber = macrosJson?.optInt("fiber") ?: 0,
            sugar = macrosJson?.optInt("sugar") ?: 0
        )

        val microsJson = json.optJSONObject("micros")
        val micros = MicroNutrients(
            vitaminA = microsJson?.optDouble("vitaminA") ?: 0.0,
            vitaminC = microsJson?.optDouble("vitaminC") ?: 0.0,
            vitaminD = microsJson?.optDouble("vitaminD") ?: 0.0,
            iron = microsJson?.optDouble("iron") ?: 0.0,
            calcium = microsJson?.optDouble("calcium") ?: 0.0,
            sodium = microsJson?.optDouble("sodium") ?: 0.0,
            potassium = microsJson?.optDouble("potassium") ?: 0.0
        )

        return MealLog(
            userRequest = originalText,
            aiResponse = json.optString("aiResponse", "Logged."),
            macros = macros,
            micros = micros
        )
    }

}
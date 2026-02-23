package com.example.aicalorietracker.network

import android.graphics.BitmapFactory
import com.example.aicalorietracker.BuildConfig
import com.example.aicalorietracker.local.MacroNutrients
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.local.MicroNutrients
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

class AiService {

    private val model = GenerativeModel(

        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {

            temperature = 0.2f
            topK = 32
            topP = 0.95f
            responseMimeType = "application/json"


        })

    suspend fun analyseMeal(localPath: String?, userText: String): Result<MealLog> {
        return try {
            val prompt = """
Analyze this meal description: "$userText".

Return ONLY valid JSON in this exact format:
{
  "aiResponse": "",
  "calories": 0,
  "macros": {
    "protein": 0,
    "carbs": 0,
    "fat": 0,
    "fiber": 0,
    "sugar": 0
  },
  "micros": {
    "vitaminA": 0.0,
    "vitaminC": 0.0,
    "vitaminD": 0.0,
    "iron": 0.0,
    "calcium": 0.0,
    "sodium": 0.0,
    "potassium": 0.0
  }
}

Estimate values when needed.
If input is not food, return all zeros and a polite aiResponse.
""".trimIndent()

            val inputContent = content{
                if (localPath != null){
                    val bitmap = BitmapFactory.decodeFile(localPath)
                    image(bitmap)
                    text(prompt)
                }
                    else{
                        text(prompt)
                }


            }
            val response = model.generateContent(inputContent)
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
                "calories", 0
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
            micros = micros,
        )
    }

}
package com.example.aicalorietracker.network

import android.R.attr.apiKey
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.example.aicalorietracker.BuildConfig
import com.example.aicalorietracker.local.MacroNutrients
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.local.MicroNutrients
import org.json.JSONObject
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Tool
import com.google.genai.types.GoogleSearch
import com.google.genai.types.Content
import com.google.genai.types.Part
import com.google.genai.types.Blob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AiService {


    private val searchTool = Tool.builder()
        .googleSearch(GoogleSearch.builder().build())
        .build()

    private val config = GenerateContentConfig.builder()
        .temperature(0.2f)
        .topK(32f)
        .topP(0.95f)
        .tools(listOf(searchTool))
        .build()

    suspend fun analyseMeal(apiKey:String,localPath: String?, userText: String): Result<MealLog> = withContext(Dispatchers.IO) {
        return@withContext try {
             val client = Client.builder()
                .apiKey(apiKey)
                .build()


            val prompt = """
Analyze this meal description: "$userText".

RULES:
1. For generic foods, homemade meals, and produce, estimate nutrition from internal memory. DO NOT search the web.
2. ONLY use Google Search if the input contains a specific branded/packaged product to find its exact nutritional label.
3. If the input is not food, return all zeros and a polite aiResponse.

Return ONLY raw, valid JSON in this exact format (no markdown blocks or backticks):
{
  "shortNameOfMeal": ""
  "aiResponse": "Brief summary",
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
""".trimIndent()

            val contentBuilder = Content.builder().role("user")

            if (localPath != null) {
                val bitmap = BitmapFactory.decodeFile(localPath)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()

                contentBuilder.parts(listOf(
                    Part.builder().inlineData(Blob.builder().data(byteArray).mimeType("image/jpeg").build()).build(),
                    Part.builder().text(prompt).build()
                ))
            } else {
                contentBuilder.parts(listOf(Part.builder().text(prompt).build()))
            }

            val response = client.models.generateContent(
                "gemini-2.5-flash",
                contentBuilder.build(),
                config
            )

            val rawString = response.text() ?: throw Exception("Empty response from AI")

            val cleanedJsonString = rawString
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val resultMeal = parseJsonToMealLog(cleanedJsonString, userText)
            Result.success(resultMeal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseJsonToMealLog(jsonString: String, originalText: String): MealLog {
        val json = JSONObject(jsonString)

        val macrosJson = json.optJSONObject("macros")
        val macros = MacroNutrients(
            calories = json.optInt("calories", 0),
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
package com.example.aicalorietracker.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.aicalorietracker.local.MacroNutrients
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.local.MicroNutrients
import com.google.genai.Client
import com.google.genai.types.Blob
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.GoogleSearch
import com.google.genai.types.Part
import com.google.genai.types.Tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class AiService {


    private val searchTool = Tool.builder().googleSearch(GoogleSearch.builder().build()).build()

    private val config = GenerateContentConfig.builder().temperature(0.2f).topK(32f).topP(0.95f)
//        .tools(listOf(searchTool))
        .build()

    suspend fun analyseMeal(
        apiKey: String, localPath: String?, userText: String, modelName: String
    ): Result<MealLog> = withContext(Dispatchers.IO) {
        return@withContext try {
            val client = Client.builder().apiKey(apiKey).build()


            val prompt = """
Analyze this meal and estimate its nutrition from the quantity provided:

"$userText"

Rules:
- Return EVERY field shown below. Never omit fields or return null.
- Use reasonable nutritional estimates for the stated quantity. If quantity is missing, use a standard serving.
- If the input is not food, return 0 for all nutrients.
- Return numbers only, without units, text, ranges, or percentages.
- Return ONLY valid JSON. No markdown or extra text.

Units:
calories = kcal
protein, carbs, fat, fiber, sugar = g
vitaminA = mcg RAE
vitaminC, iron, calcium, sodium, potassium = mg
vitaminD = mcg

{
  "shortNameOfMeal": "",
  "aiResponse": "",
  "calories": 0,
  "macros": {
    "protein": 0.0,
    "carbs": 0.0,
    "fat": 0.0,
    "fiber": 0.0,
    "sugar": 0.0
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


            Log.d("Ai response", "")

            val contentBuilder = Content.builder().role("user")

            if (localPath != null) {
                val bitmap = BitmapFactory.decodeFile(localPath)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()

                contentBuilder.parts(
                    listOf(
                        Part.builder().inlineData(
                            Blob.builder().data(byteArray).mimeType("image/jpeg").build()
                        ).build(), Part.builder().text(prompt).build()
                    )
                )
            } else {
                contentBuilder.parts(listOf(Part.builder().text(prompt).build()))
            }

            val response = client.models.generateContent(
                modelName, contentBuilder.build(), config
            )


            val rawString = response.text() ?: throw Exception("Empty response from AI")

            val cleanedJsonString = rawString.replace("```json", "").replace("```", "").trim()

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
            shortName = json.optString("shortNameOfMeal", originalText))

    }
}

data class GeminiModelOption(
    val name: String, val displayName: String, val description: String
)
package com.myurrrjj.trackabite.network

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.IOException
import com.myurrrjj.trackabite.local.MacroNutrients
import com.myurrrjj.trackabite.local.MealLog
import com.myurrrjj.trackabite.local.MicroNutrients
import com.google.genai.Client
import com.google.genai.types.Blob
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.GoogleSearch
import com.google.genai.types.Part
import com.google.genai.types.Tool
import com.myurrrjj.trackabite.local.AiMealResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import kotlin.coroutines.cancellation.CancellationException


private const val MAX_INLINE_IMAGE_BYTES = 64L * 1024L * 1024L
class AiService {


    private val searchTool = Tool.builder().googleSearch(GoogleSearch.builder().build()).build()

    private val config = GenerateContentConfig.builder().temperature(0.2f).topK(32f).topP(0.95f).responseMimeType("application/json")
//        .tools(listOf(searchTool))
        .build()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun loadJpegBytes(localPath: String): ByteArray {
        val file = File(localPath)

        if (!file.isFile || !file.canRead()) {
            throw IOException("AI image file is missing or unreadable.")
        }

        if (file.length() < 4L) {
            throw IOException("AI image file is empty or invalid.")
        }
        if (file.length() > MAX_INLINE_IMAGE_BYTES) {
            throw IOException("AI image exceeds the supported inline image size.")
        }

        val bytes = file.readBytes()

        if (
            bytes.size < 4 ||
            bytes[0] != 0xFF.toByte() ||
            bytes[1] != 0xD8.toByte()
        ) {
            throw IOException("AI image is not a valid JPEG file.")
        }

        return bytes
    }

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


//            Log.d("Ai response", "")

            val contentBuilder = Content.builder().role("user")

            if (localPath != null) {
                val imageBytes = loadJpegBytes(localPath)

                val imagePart = Part.builder().inlineData(
                    Blob.builder()
                        .data(imageBytes)
                        .mimeType("image/jpeg")
                        .build()
                ).build()

                val textPart = Part.builder()
                    .text(prompt)
                    .build()

                contentBuilder.parts(listOf(imagePart, textPart))
            } else {
                contentBuilder.parts(listOf(Part.builder().text(prompt).build()))
            }

            val response = client.models.generateContent(
                modelName, contentBuilder.build(), config
            )


            val rawString = response.text() ?: throw Exception("Empty response from AI")

            val parsedAiResponse = jsonParser.decodeFromString<AiMealResponse>(rawString)

//            val cleanedJsonString = rawString.replace("```json", "").replace("```", "").trim()
//
//            val resultMeal = parseJsonToMealLog(cleanedJsonString, userText)


            val macros = MacroNutrients(
                calories = parsedAiResponse.calories,
                protein = parsedAiResponse.macros.protein.toInt(),
                carbs = parsedAiResponse.macros.carbs.toInt(),
                fat = parsedAiResponse.macros.fat.toInt(),
                fiber = parsedAiResponse.macros.fiber.toInt(),
                sugar = parsedAiResponse.macros.sugar.toInt()
            )

            val micros = MicroNutrients(
                vitaminA = parsedAiResponse.micros.vitaminA.toDouble(),
                vitaminC = parsedAiResponse.micros.vitaminC.toDouble(),
                vitaminD = parsedAiResponse.micros.vitaminD.toDouble(),
                iron = parsedAiResponse.micros.iron.toDouble(),
                calcium = parsedAiResponse.micros.calcium.toDouble(),
                sodium = parsedAiResponse.micros.sodium.toDouble(),
                potassium = parsedAiResponse.micros.potassium.toDouble()
            )

            val resultMeal = MealLog(
                userRequest = userText,
                shortName = parsedAiResponse.shortNameOfMeal,
                aiResponse = parsedAiResponse.aiResponse,
                macros = macros,
                micros = micros,
            )
            Result.success(resultMeal)
        } catch (e: CancellationException) {
            throw e
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
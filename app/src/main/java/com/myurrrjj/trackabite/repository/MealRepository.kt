package com.myurrrjj.trackabite.repository


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.google.genai.Client
import com.google.genai.types.ListModelsConfig
import com.myurrrjj.trackabite.local.MealDao
import com.myurrrjj.trackabite.local.MealLog
import com.myurrrjj.trackabite.local.SavedMeal
import com.myurrrjj.trackabite.local.SavedMealDao
import com.myurrrjj.trackabite.network.AiService
import com.myurrrjj.trackabite.network.GeminiModelOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.math.sqrt

interface MealRepository {

    fun getMealHistory(): Flow<List<MealLog>>

    suspend fun addMealLog(
        localPath: String?, userText: String, date: LocalDate, modelName: String
    ): Result<MealLog>

    suspend fun deleteLog(mealLog: MealLog)

    fun getMealsForDay(startTime: Long, endTime: Long): Flow<List<MealLog>>
    fun getCaloriesForDay(startTime: Long, endTime: Long): Flow<Int?>
    suspend fun updateMeal(mealLog: MealLog)
    fun getSavedMeals(): Flow<List<SavedMeal>>
    suspend fun saveMealToFavourites(mealLog: MealLog)
    suspend fun deleteSavedMeal(savedMeal: SavedMeal)
    suspend fun quickLogSavedMeal(savedMeal: SavedMeal, date: LocalDate): Result<MealLog>

    suspend fun getAvailableModels(): Result<List<GeminiModelOption>>
    fun getMealsForDateRange(startTime: Long, endTime: Long): Flow<List<MealLog>>
}


private const val maxMegapixels = 12000000L
private fun calculateDimensions(width: Int, height: Int, maxPixels: Long): Pair<Int, Int> {
    require(width > 0 && height > 0) {
        "Image Dimensions must be positive"
    }
    require(maxPixels > 0) {
        "Pixel budget must be positive"
    }

    val pixels = width.toLong() * height.toLong()
    if (pixels <= maxPixels) {
        return width to height

    }

    val scale = sqrt(maxPixels.toDouble() / pixels.toDouble())
    val newWidth = (width * scale).toInt().coerceAtLeast(1)
    val newHeight = (height * scale).toInt().coerceAtLeast(1)

    return newWidth to newHeight

}


private fun readImageDimensions(
    context: Context, uri: Uri
): Pair<Int, Int>? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        inputStream.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        val width = options.outWidth
        val height = options.outHeight

        if (width <= 0 || height <= 0) {
            null
        } else {
            width to height
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


private fun calculateInSampleSize(
    sourceWidth: Int, sourceHeight: Int, maxPixels: Long
): Int {
    require(sourceWidth > 0 && sourceHeight > 0)
    require(maxPixels > 0)

    var sampleSize = 1

    fun ceilDiv(value: Int, divisor: Int): Long {
        return (value.toLong() + divisor.toLong() - 1L) / divisor.toLong()
    }

    while (ceilDiv(sourceWidth, sampleSize) * ceilDiv(sourceHeight, sampleSize) > maxPixels) {
        if (sampleSize > Int.MAX_VALUE / 2) {
            throw IOException("Image dimensions are too large to decode safely.")
        }

        sampleSize *= 2
    }

    return sampleSize
}

/**
 * Reads EXIF orientation.
 *
 * If metadata is missing or unreadable, normal orientation is assumed.
 */
private fun readExifOrientation(
    context: Context, uri: Uri
): Int {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ExifInterface(inputStream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (e: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }
}


private fun applyExifOrientation(
    bitmap: Bitmap, orientation: Int
): Bitmap {
    val matrix = Matrix()

    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
            matrix.setScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_ROTATE_180 -> {
            matrix.setRotate(180f)
        }

        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            matrix.setRotate(180f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_ROTATE_90 -> {
            matrix.setRotate(90f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_ROTATE_270 -> {
            matrix.setRotate(-90f)
        }

        else -> return bitmap
    }

    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )
}


private fun decodeAiBitmap(
    context: Context, uri: Uri
): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

        val source = ImageDecoder.createSource(
            context.contentResolver, uri
        )

        ImageDecoder.decodeBitmap(source) { decoder, imageInfo, _ ->

            val (targetWidth, targetHeight) = calculateDimensions(
                imageInfo.size.width, imageInfo.size.height, maxMegapixels
            )

            decoder.setTargetSize(targetWidth, targetHeight)


            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }

    } else {
        decodeAiBitmapLegacy(context, uri)
    }
}

private const val MAX_THUMBNAIL_DIMENSION = 512

private fun decodeAiBitmapLegacy(
    context: Context, uri: Uri
): Bitmap {
    val dimensions =
        readImageDimensions(context, uri) ?: throw IOException("Unable to read image dimensions.")

    val (sourceWidth, sourceHeight) = dimensions

    val sampleSize = calculateInSampleSize(
        sourceWidth, sourceHeight, maxMegapixels
    )

    val orientation = readExifOrientation(context, uri)

    var bitmap: Bitmap? = null

    try {
        bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }

            BitmapFactory.decodeStream(inputStream, null, options)

        } ?: throw IOException("Unable to decode selected image.")


        if (bitmap.width.toLong() * bitmap.height.toLong() > maxMegapixels) {
            val (targetWidth, targetHeight) = calculateDimensions(
                bitmap.width, bitmap.height, maxMegapixels
            )

            val scaledBitmap = bitmap.scale(targetWidth, targetHeight)

            if (scaledBitmap !== bitmap) {
                bitmap.recycle()
            }

            bitmap = scaledBitmap
        }

        val orientedBitmap = applyExifOrientation(
            bitmap, orientation
        )

        if (orientedBitmap !== bitmap) {
            bitmap.recycle()
        }

        bitmap = orientedBitmap

        return orientedBitmap

    } catch (e: Throwable) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }

        throw e
    }
}

private fun createMealThumbnail(
    sourceBitmap: Bitmap
): Bitmap {
    val largestDimension = maxOf(
        sourceBitmap.width,
        sourceBitmap.height
    )

    if (largestDimension <= MAX_THUMBNAIL_DIMENSION) {
        return sourceBitmap
    }

    val scale = MAX_THUMBNAIL_DIMENSION.toFloat() /
            largestDimension.toFloat()

    val targetWidth = (
            sourceBitmap.width * scale
            ).roundToInt().coerceAtLeast(1)

    val targetHeight = (
            sourceBitmap.height * scale
            ).roundToInt().coerceAtLeast(1)

    return sourceBitmap.scale(targetWidth, targetHeight)
}

//fun processAndSaveImage(context: Context, uriString: String): String? {
//    return try {
//        val uri = uriString.toUri()
//        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
//        val options = BitmapFactory.Options().apply {
//            inSampleSize = 2
//        }
//        val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
//        inputStream.close()
//        if (bitmap == null) return null
//        val filename = "meal_${System.currentTimeMillis()}.jpg"
//        val file = File(context.filesDir, filename)
//        FileOutputStream(file).use { out ->
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
//        }
//
//        bitmap.recycle()
//
//        file.absolutePath
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}

class OfflineMealRepository(
    private val aiService: AiService,
    private val mealDao: MealDao,
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedMealDao: SavedMealDao
) : MealRepository {

    private suspend fun deleteImageIfUnreferenced(imagePath: String?) {
        if (imagePath.isNullOrBlank()) return

        try {
            val imageFile = File(imagePath).canonicalFile
            val filesDirectory = context.filesDir.canonicalFile
            val thumbnailDirectory = File(
                filesDirectory,
                "meal_thumbnails"
            ).canonicalFile

            if (
                imageFile.parentFile != filesDirectory &&
                imageFile.parentFile != thumbnailDirectory
            ) {
                return
            }

            if (
                !imageFile.name.startsWith("meal_") ||
                !imageFile.name.endsWith(".jpg", ignoreCase = true)
            ) {
                return
            }

            if (!imageFile.isFile) return

            if (
                mealDao.getImageReferenceCount(imagePath) == 0 &&
                !imageFile.delete()
            ) {
                Log.w("MealRepository", "Unable to delete unreferenced image.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("MealRepository", "Image cleanup failed.", e)
        }
    }


    override fun getMealHistory(): Flow<List<MealLog>> {
        return mealDao.getAllMeals()
    }

    override suspend fun addMealLog(
        imageUri: String?,
        userText: String,
        date: LocalDate,
        modelName: String
    ): Result<MealLog> = withContext(Dispatchers.IO) {

        var preparedImages: PreparedMealImages? = null
        var thumbnailCommitted = false

        try {
            val apiKey = userPreferencesRepository.getApiKey()
                ?: throw IllegalStateException(
                    "API Key is missing. Please set it in Settings."
                )

            preparedImages = imageUri?.let {
                prepareMealImages(context, it)
            }

            imageUri?.let {
                deleteCameraTempImage(context, it)
            }

            val analysisResult = aiService.analyseMeal(
                apiKey = apiKey,
                localPath = preparedImages?.aiImageFile?.absolutePath,
                userText = userText,
                modelName = modelName
            )

            val analysedMeal = analysisResult.getOrElse {
                return@withContext Result.failure(it)
            }

            val nowTime = LocalTime.now()

            val correctTimeStamp = date
                .atTime(nowTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val adjustedMeal = analysedMeal.copy(
                timeStamp = correctTimeStamp,
                imagePath = preparedImages?.thumbnailFile?.absolutePath
            )

            withContext(NonCancellable) {
                mealDao.insertMeal(adjustedMeal)
                thumbnailCommitted = true
            }

            Result.success(adjustedMeal)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            preparedImages?.aiImageFile?.let { file ->
                if (file.exists() && !file.delete()) {
                    Log.w("MealRepository", "Unable to delete temporary AI image.")
                }
            }

            if (!thumbnailCommitted) {
                preparedImages?.thumbnailFile?.let { file ->
                    if (file.exists() && !file.delete()) {
                        Log.w("MealRepository", "Unable to roll back meal thumbnail.")
                    }
                }
            }

            imageUri?.let {
                deleteCameraTempImage(context, it)
            }
        }
    }

    override suspend fun deleteLog(mealLog: MealLog) {
        withContext(Dispatchers.IO) {
            mealDao.deleteMeal(mealLog)
            deleteImageIfUnreferenced(mealLog.imagePath)
        }
    }

    override fun getMealsForDay(
        startTime: Long, endTime: Long
    ): Flow<List<MealLog>> {
        return mealDao.getMealsForDay(startTime, endTime)

    }

    override fun getCaloriesForDay(
        startTime: Long, endTime: Long
    ): Flow<Int?> {
        return mealDao.getTotalCaloriesForDay(startTime, endTime)
    }

    override fun getSavedMeals(): Flow<List<SavedMeal>> {
        return savedMealDao.getAllSavedMeals()
    }

    override suspend fun updateMeal(mealLog: MealLog) = withContext(Dispatchers.IO) {
        mealDao.updateMeal(mealLog)
    }

    override fun getMealsForDateRange(startTime: Long, endTime: Long): Flow<List<MealLog>> {
        return mealDao.getMealsForDateRange(startTime, endTime)
    }

    override suspend fun saveMealToFavourites(mealLog: MealLog) {
        withContext(Dispatchers.IO) {
            val savedMeal = SavedMeal(
                userRequest = mealLog.userRequest,
                shortName = mealLog.shortName,
                aiResponse = mealLog.aiResponse,
                imagePath = mealLog.imagePath,
                macros = mealLog.macros,
                micros = mealLog.micros,
                quantity = mealLog.quantity,
                frequency = 0
            )
            savedMealDao.saveMeal(savedMeal)
        }
    }

    override suspend fun deleteSavedMeal(savedMeal: SavedMeal) {
        withContext(Dispatchers.IO) {
            savedMealDao.deleteSavedMeal(savedMeal)
            deleteImageIfUnreferenced(savedMeal.imagePath)
        }
    }

    override suspend fun quickLogSavedMeal(
        savedMeal: SavedMeal,
        date: LocalDate
    ): Result<MealLog> {
        return withContext(Dispatchers.IO) {
            try {
                val nowTime = LocalTime.now()

                val correctTimeStamp = date
                    .atTime(nowTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val newMealLog = MealLog(
                    timeStamp = correctTimeStamp,
                    shortName = savedMeal.shortName,
                    userRequest = savedMeal.userRequest,
                    aiResponse = savedMeal.aiResponse,
                    imagePath = savedMeal.imagePath,
                    macros = savedMeal.macros,
                    micros = savedMeal.micros,
                    quantity = 1f
                )

                mealDao.insertMeal(newMealLog)

                savedMealDao.saveMeal(
                    savedMeal.copy(frequency = savedMeal.frequency + 1)
                )

                Result.success(newMealLog)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    override suspend fun getAvailableModels(): Result<List<GeminiModelOption>> = withContext(
        Dispatchers.IO
    ) {
        try {
            val apiKey =
                userPreferencesRepository.getApiKey() ?: throw Exception("API Key is missing")
            val client = Client.builder().apiKey(apiKey).build()

            val models = client.models.list(ListModelsConfig.builder().build()).asSequence()
                .filter { model ->
                    model.supportedActions().orElse(emptyList()).contains("generateContent")
                }.filter { model ->
                    val name = model.name().orElse("").lowercase()
                    name.contains("gemini") && !name.contains("nano")
                    (name.contains("flash") || name.contains("pro"))
//                    !name.contains("Nano")

                }.mapNotNull { model ->
                    val name = model.name().orElse(null) ?: return@mapNotNull null

                    GeminiModelOption(
                        name = name.removePrefix("models/"),
                        displayName = model.displayName().orElse(name),
                        description = model.description().orElse("")
                    )
                }.toList()

            Result.success(models)


        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}


private const val AI_JPEG_QUALITY = 90
private const val THUMBNAIL_JPEG_QUALITY = 80

private data class PreparedMealImages(
    val aiImageFile: File,
    val thumbnailFile: File
)


private fun createAiImageFile(context: Context): File {
    val directory = context.cacheDir

    if (!directory.exists() && !directory.mkdirs()) {
        throw IOException("Unable to create AI image cache directory.")
    }

    if (!directory.isDirectory) {
        throw IOException("AI image cache path is not a directory.")
    }

    return File(directory, "meal_ai_${UUID.randomUUID()}.jpg")
}


private fun createThumbnailFile(context: Context): File {
    val directory = File(context.filesDir, "meal_thumbnails")

    if (!directory.exists() && !directory.mkdirs()) {
        throw IOException("Unable to create meal thumbnail directory.")
    }

    if (!directory.isDirectory) {
        throw IOException("Meal thumbnail path is not a directory.")
    }

    return File(directory, "meal_${UUID.randomUUID()}.jpg")
}


private fun writeJpegAtomically(
    bitmap: Bitmap,
    destination: File,
    quality: Int
) {
    require(quality in 0..100)

    val parentDirectory = destination.parentFile
        ?: throw IOException("Destination has no parent directory.")

    if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
        throw IOException("Unable to create destination directory.")
    }

    if (destination.exists()) {
        throw IOException("Destination file already exists.")
    }

    val temporaryFile = File.createTempFile(
        "meal_",
        ".tmp",
        parentDirectory
    )

    try {
        FileOutputStream(temporaryFile).use { output ->
            val compressed = bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                output
            )

            if (!compressed) {
                throw IOException("JPEG compression failed.")
            }

            output.flush()
            output.fd.sync()
        }

        if (!temporaryFile.renameTo(destination)) {
            throw IOException("Unable to finalize JPEG file.")
        }
    } finally {
        if (temporaryFile.exists()) {
            temporaryFile.delete()
        }
    }
}


private fun prepareMealImages(
    context: Context,
    uriString: String
): PreparedMealImages {
    val uri = uriString.toUri()

    val aiImageFile = createAiImageFile(context)
    val thumbnailFile = createThumbnailFile(context)

    var aiBitmap: Bitmap? = null
    var thumbnailBitmap: Bitmap? = null
    var preparationCompleted = false

    try {
        val decodedBitmap = decodeAiBitmap(context, uri)
        aiBitmap = decodedBitmap

        val generatedThumbnail = createMealThumbnail(decodedBitmap)
        thumbnailBitmap = generatedThumbnail

        writeJpegAtomically(
            bitmap = decodedBitmap,
            destination = aiImageFile,
            quality = AI_JPEG_QUALITY
        )

        writeJpegAtomically(
            bitmap = generatedThumbnail,
            destination = thumbnailFile,
            quality = THUMBNAIL_JPEG_QUALITY
        )

        preparationCompleted = true

        return PreparedMealImages(
            aiImageFile = aiImageFile,
            thumbnailFile = thumbnailFile
        )
    } finally {
        if (!preparationCompleted) {
            aiImageFile.delete()
            thumbnailFile.delete()
        }

        thumbnailBitmap?.let { bitmap ->
            if (bitmap !== aiBitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }

        aiBitmap?.let { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
}

private fun deleteCameraTempImage(
    context: Context,
    uriString: String
) {
    try {
        val uri = uriString.toUri()

        if (
            uri.scheme != "content" ||
            uri.authority != "${context.packageName}.provider"
        ) {
            return
        }

        val pathSegments = uri.pathSegments

        if (
            pathSegments.size != 2 ||
            pathSegments[0] != "temp_images"
        ) {
            return
        }

        val fileName = pathSegments[1]

        if (
            !fileName.startsWith("temp_image_") ||
            !fileName.endsWith(".jpg", ignoreCase = true)
        ) {
            return
        }

        val cacheDirectory = context.cacheDir.canonicalFile
        val candidate = File(cacheDirectory, fileName).canonicalFile

        if (candidate.parentFile != cacheDirectory) {
            return
        }

        if (candidate.exists() && !candidate.delete()) {
            Log.w("MealRepository", "Unable to delete camera temporary image.")
        }
    } catch (e: Exception) {
        Log.w("MealRepository", "Camera temporary image cleanup failed.", e)
    }
}
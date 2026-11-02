package com.myurrrjj.trackabite.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myurrrjj.trackabite.CalorieTrackerApplication
import com.myurrrjj.trackabite.local.MacroNutrients
import com.myurrrjj.trackabite.local.MealLog
import com.myurrrjj.trackabite.local.MicroNutrients
import com.myurrrjj.trackabite.local.SavedMeal
import com.myurrrjj.trackabite.network.GeminiModelOption
import com.myurrrjj.trackabite.repository.HealthRepository
import com.myurrrjj.trackabite.repository.MealRepository
import com.myurrrjj.trackabite.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

fun isSameDay(date: LocalDate, timestamp: Long): Boolean {
    val logDate =
        java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return date == logDate
}


class MealViewModel(
    private val repository: MealRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val healthRepository: HealthRepository
) : ViewModel() {
    private val _refreshHealthData = MutableStateFlow(System.currentTimeMillis())
    private val dayFlowCache = mutableMapOf<LocalDate, Flow<MealUiState>>()
//    private val dayFlowCache = object : LruCache<LocalDate, Flow<MealUiState>>(7) {}

    private val _availableModels = MutableStateFlow<List<GeminiModelOption>>(emptyList())
    val availableModels: StateFlow<List<GeminiModelOption>> = _availableModels.asStateFlow()

    private val _selectedModel = MutableStateFlow<GeminiModelOption?>(null)

    val selectedModel: StateFlow<GeminiModelOption?> = _selectedModel.asStateFlow()
    val currentStreak : StateFlow<Int> = repository.getMealHistory()
        .map { meals->
            val uniqueDates = meals.map {
                Instant.ofEpochMilli(it.timeStamp).atZone(ZoneId.systemDefault()).toLocalDate()

            }.toSet()
            var streak = 0
            var checkDate = LocalDate.now()
            if (!uniqueDates.contains(checkDate)){
                checkDate = checkDate.minusDays(1)

            }
            while (uniqueDates.contains(checkDate)){
                streak++
                checkDate = checkDate.minusDays(1)


            }
            streak
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),0)
    val hasApiKey: StateFlow<Boolean> =
        preferencesRepository.apiKey.map { !it.isNullOrBlank() }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            !preferencesRepository.getApiKey().isNullOrBlank()
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _pendingMeals = MutableStateFlow<List<MealLog>>(emptyList())
    private val _targetCalories = MutableStateFlow(preferencesRepository.getTargetCalories())

    val savedMeals: StateFlow<List<SavedMeal>> = repository.getSavedMeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getApiKey(): String? = preferencesRepository.getApiKey()

    fun saveApiKey(key: String) {
        preferencesRepository.saveApiKey(key)
    }

    fun refreshHealthData() {
        _refreshHealthData.value = System.currentTimeMillis()
    }


    val today: LocalDate
        get() = LocalDate.now()


    init {
        preferencesRepository.apiKey.onEach { key ->
            if (!key.isNullOrBlank()) {
                loadAvailableModels()
            }
        }.launchIn(viewModelScope)
    }


    fun loadAvailableModels() {
        viewModelScope.launch {
            repository.getAvailableModels().onSuccess { models ->
                _errorMessage.value = null
                _availableModels.value = models

                val savedModelName = preferencesRepository.getSelectedModelName()

//                val current = _selectedModel.value
                _selectedModel.value =
                    models.find { it.name == savedModelName } ?: pickDefaultModel(models)


            }.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage ?: "Failed to load AI models"
            }
        }
    }


    private fun pickDefaultModel(models: List<GeminiModelOption>): GeminiModelOption? {
        val preferred = listOf("gemini-3.8-flash", "gemini-3.5-flash-lite")
        preferred.forEach { wanted ->
            models.firstOrNull { it.name.equals(wanted, ignoreCase = true) }?.let { return it }
        }
        models.firstOrNull { it.name.contains("flash-lite") }?.let { return it }
        models.firstOrNull { it.name.contains("flash") && !it.name.contains("pro") }
            ?.let { return it }
        return models.firstOrNull()
    }

    fun selectModel(model: GeminiModelOption) {
        _selectedModel.value = model
        preferencesRepository.saveSelectedModelName(model.name)

    }

    fun getDayFlow(date: LocalDate): Flow<MealUiState> {
        return dayFlowCache.getOrPut(date) {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay =
                date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            combine(
                repository.getMealsForDay(startOfDay, endOfDay),
                _targetCalories,
                _pendingMeals,
                _errorMessage,
                _refreshHealthData
            ) { dbMeals, target, pendingMeals, error, _ ->

                val relevantPending = pendingMeals.filter { isSameDay(date, it.timeStamp) }

                val allMeals = relevantPending + dbMeals
                val currentTotalCalories = allMeals.sumOf { it.effectiveCalories }
                val energyBurn = healthRepository.getEnergyBurnForDay(date)

                val profile = preferencesRepository.profile.value
                val weight = profile?.weightKg?:60f
                val defaultCarbs = (target * 0.50f / 4f).toInt()
                val defaultFat = (target * 0.30f / 9f).toInt()

                val proteinFromCals = (target * 0.20f / 4f).toInt()
                val proteinFromWeight = (weight * 0.8f).toInt()
                val defaultProtein = maxOf(proteinFromCals, proteinFromWeight)
                val defaultFiber = (target / 1000f * 14f).toInt()
                val defaultSugar = (target * 0.10f / 4f).toInt()

                MealUiState(
                    meals = allMeals,
                    totalCalories = currentTotalCalories,
                    activeCalories = energyBurn.activeCalories,
                    basalCalories = energyBurn.basalCalories,
                    isBasalEstimated = energyBurn.isEstimated,
                    isLoading = relevantPending.isNotEmpty(),
                    errorMessage = error,
                    currentDate = date,
                    targetCalories = target,
                    targetProtein = preferencesRepository.getTargetProtein().takeIf { it > 0 } ?: defaultProtein,
                    targetCarbs = preferencesRepository.getTargetCarbs().takeIf { it > 0 } ?: defaultCarbs,
                    targetFat = preferencesRepository.getTargetFat().takeIf { it > 0 } ?: defaultFat,
                    targetFiber = preferencesRepository.getTargetFiber().takeIf { it > 0 } ?: defaultFiber,
                    targetSugar = preferencesRepository.getTargetSugar().takeIf { it > 0 } ?: defaultSugar)
            }.flowOn(Dispatchers.Default)
        }
    }

    fun updateAllTargets(cal: Int, pro: Int, carb: Int, fat: Int, fib: Int, sug: Int) {
       preferencesRepository.updateTargetCalories(cal)
        preferencesRepository.updateTargetProtein(pro)
        preferencesRepository.updateTargetCarbs(carb)
        preferencesRepository.updateTargetFat(fat)
        preferencesRepository.updateTargetFiber(fib)
        preferencesRepository.updateTargetSugar(sug)
        _targetCalories.value = cal
    }

    fun analyseAndAddMeal(imageUri: Uri?, userText: String, date: LocalDate) {
        if (userText.isBlank() && imageUri == null) return

        val nowTime = LocalTime.now()
        val optimisticTimestamp =
            date.atTime(nowTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val modelName = _selectedModel.value?.name ?: return


        val tempId = -Random.nextInt(1, 100000)

        val optimisticMeal = MealLog(
            id = tempId,
            timeStamp = optimisticTimestamp,
            shortName = "Meal",
            userRequest = userText.ifBlank { "Image Analysis" },
            aiResponse = "Analysing...",
            macros = MacroNutrients(),
            micros = MicroNutrients(),
            imagePath = imageUri?.toString(),
            quantity = 1f,
        ).apply { isAnalysing = true }

        _pendingMeals.value = listOf(optimisticMeal) + _pendingMeals.value
        viewModelScope.launch {
            _errorMessage.value = null
            val uriString = imageUri?.toString()

            val result = repository.addMealLog(uriString, userText, date, modelName)

            _pendingMeals.value = _pendingMeals.value.filter { it.id != tempId }

            result.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage ?: "Unknown Error Occurred"
            }
        }
    }

    fun deleteMeal(meal: MealLog) {
        if (meal.isAnalysing) {
            _pendingMeals.value = _pendingMeals.value.filter { it.id != meal.id }
        } else {
            viewModelScope.launch {
                repository.deleteLog(meal)
            }
        }
    }

    fun errorShown() {
        _errorMessage.value = null
    }

    fun quickLogSavedMeal(savedMeal: SavedMeal, date: LocalDate) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.quickLogSavedMeal(savedMeal, date)
            result.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage ?: "Error Logging Saved Meal"
            }
        }
    }

    fun saveMealToFavorites(mealLog: MealLog) {
        viewModelScope.launch {
            repository.saveMealToFavourites(mealLog)
        }
    }

    fun deleteSavedMeal(savedMeal: SavedMeal) {
        viewModelScope.launch {
            repository.deleteSavedMeal(savedMeal)
        }
    }

    fun updateMealQuantity(meal: MealLog, newQuantity: Float) {
        if (meal.isAnalysing) return
        val updatedMeal = meal.copy(quantity = newQuantity)
        viewModelScope.launch {
            repository.updateMeal(updatedMeal)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CalorieTrackerApplication)
                MealViewModel(
                    repository = application.container.mealRepository,
                    preferencesRepository = application.container.userPreferencesRepository,
                    healthRepository = application.container.healthRepository
                )
            }
        }
    }
}
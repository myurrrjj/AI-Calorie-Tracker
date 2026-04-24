package com.example.aicalorietracker.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aicalorietracker.CalorieTrackerApplication
import com.example.aicalorietracker.local.MacroNutrients
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.local.MicroNutrients
import com.example.aicalorietracker.local.SavedMeal
import com.example.aicalorietracker.repository.MealRepository
import com.example.aicalorietracker.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random

fun isSameDay(date: LocalDate, timestamp: Long): Boolean {
    val logDate = java.time.Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return date == logDate
}



class MealViewModel(
    private val repository: MealRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val dayFlowCache = mutableMapOf<LocalDate, Flow<MealUiState>>()


    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _pendingMeals = MutableStateFlow<List<MealLog>>(emptyList())
    private val _targetCalories = MutableStateFlow(preferencesRepository.getTargetCalories())

    val savedMeals: StateFlow<List<SavedMeal>> = repository.getSavedMeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),emptyList())
    fun getApiKey(): String? = preferencesRepository.getApiKey()

    fun saveApiKey(key: String) {
        preferencesRepository.saveApiKey(key)
    }

    val today: LocalDate
        get() = LocalDate.now()

    fun getDayFlow(date: LocalDate): Flow<MealUiState> {
        return dayFlowCache.getOrPut(date) {
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay =
                date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            combine(
                repository.getMealsForDay(startOfDay, endOfDay),
                _targetCalories, _pendingMeals,
                _errorMessage
            ) { dbMeals, target, pendingMeals, error ->

                val relevantPending = pendingMeals.filter { isSameDay(date, it.timeStamp) }

                val allMeals = relevantPending + dbMeals
                val currentTotalCalories = allMeals.sumOf { it.effectiveCalories }

                MealUiState(
                    meals = allMeals,
                    totalCalories = currentTotalCalories,
                    isLoading = relevantPending.isNotEmpty(),
                    errorMessage = error,
                    currentDate = date,
                    targetCalories = target
                )
            }.flowOn(Dispatchers.Default)
        }
    }

    fun updateTargetCalories(newTarget: Int) {
        if (newTarget in 500..10000) {
            preferencesRepository.updateTargetCalories(newTarget)
            _targetCalories.value = newTarget
        }
    }

    fun analyseAndAddMeal(imageUri: Uri?, userText: String, date: LocalDate) {
        if (userText.isBlank() && imageUri == null) return

        val nowTime = LocalTime.now()
        val optimisticTimestamp = date.atTime(nowTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val tempId = -Random.nextInt(1, 100000)

        val optimisticMeal = MealLog(
            id = tempId,
            timeStamp = optimisticTimestamp,
            userRequest = userText.ifBlank { "Image Analysis" },
            aiResponse = "Analysing...",
            macros = MacroNutrients(),
            micros = MicroNutrients(),
            imagePath = imageUri?.toString(),
            quantity =  1f
        ).apply { isAnalysing = true }

        _pendingMeals.value = listOf(optimisticMeal) + _pendingMeals.value
        viewModelScope.launch {
            _errorMessage.value = null
            val uriString = imageUri?.toString()

            val result = repository.addMealLog(uriString, userText, date)

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

    fun quickLogSavedMeal(savedMeal: SavedMeal,date: LocalDate){
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.quickLogSavedMeal(savedMeal,date)
            result.onFailure { exception->
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
                    preferencesRepository = application.container.userPreferencesRepository
                )
            }
        }
    }
}
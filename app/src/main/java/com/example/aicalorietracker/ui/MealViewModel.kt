package com.example.aicalorietracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aicalorietracker.CalorieTrackerApplication
import com.example.aicalorietracker.local.MealLog
import com.example.aicalorietracker.repository.MealRepository
//import com.google.android.material.loadingindicator.LoadingIndicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class MealUiState(
    val isLoading: Boolean = false,
    val meals: List<MealLog> = emptyList(),
    val totalCalories: Int = 0,
    val errorMessage: String? = null,
    val targetCalories:Int = 2500
)

class MealViewModel(private val repository: MealRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val today = LocalDate.now()

    private val startOfDay: Long =
        today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val endOfDay: Long =
        today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val uiState: StateFlow<MealUiState> = combine(
        repository.getMealsForDay(startOfDay, endOfDay),
        repository.getCaloriesForDay(startOfDay, endOfDay),
        _isLoading,
        _errorMessage
    ) { meals, calories, loading, error ->
        MealUiState(
            meals = meals,
            totalCalories = calories ?: 0,
            isLoading = loading,
            errorMessage = error
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MealUiState(isLoading = true)
    )

    fun analyseAndAddMeal(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.addMealLog(userText)
            result.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage ?: "Unknown Error Occurred"
            }
            result.onSuccess {
                println("DEBUG: Successfully added meal with timestamp: ${it.timeStamp}")
                println("DEBUG: Current View Range: $startOfDay to $endOfDay")
            }
            _isLoading.value =false

//            LoadingIndicator()
        }

    }

    fun deleteMeal(meal: MealLog){
        viewModelScope.launch {
            repository.deleteLog(meal)
        }
    }
    fun errorShown(){
        _errorMessage.value =null
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CalorieTrackerApplication)
                MealViewModel(repository = application.container.mealRepository)
            }
        }
    }

}
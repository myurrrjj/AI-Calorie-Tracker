package com.myurrrjj.trackabite.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myurrrjj.trackabite.CalorieTrackerApplication
import com.myurrrjj.trackabite.local.Sex
import com.myurrrjj.trackabite.repository.MealRepository
import com.myurrrjj.trackabite.repository.UserPreferencesRepository
import com.myurrrjj.trackabite.ui.analytics.chart.DailyAggregateData
import com.myurrrjj.trackabite.ui.analytics.chart.MacroType
import com.myurrrjj.trackabite.ui.analytics.chart.TimeRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AnalyticsViewModel(
    private val repository: MealRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun getTargetForMacro(macro: MacroType): Float {
        val targetCals = preferencesRepository.getTargetCalories().takeIf { it > 0 } ?: 2000
        val profile = preferencesRepository.profile.value
        val weight = profile?.weightKg ?: 60f

        return when (macro) {
            MacroType.CALORIES -> targetCals.toFloat()
            MacroType.PROTEIN -> {
                val saved = preferencesRepository.getTargetProtein()
                if (saved > 0) saved.toFloat()
                else maxOf((targetCals * 0.20f / 4f).toInt(), (weight * 0.8f).toInt()).toFloat()
            }
            MacroType.CARBS -> {
                val saved = preferencesRepository.getTargetCarbs()
                if (saved > 0) saved.toFloat() else (targetCals * 0.50f / 4f).toInt().toFloat()
            }
            MacroType.FAT -> {
                val saved = preferencesRepository.getTargetFat()
                if (saved > 0) saved.toFloat() else (targetCals * 0.30f / 9f).toInt().toFloat()
            }
            MacroType.FIBER -> {
                val saved = preferencesRepository.getTargetFiber()
                if (saved > 0) saved.toFloat()
                else if (profile != null) {
                    when (profile.sex) {
                        Sex.MALE -> if (profile.age <= 50) 38f else 30f
                        Sex.FEMALE -> if (profile.age <= 50) 25f else 21f
                    }
                } else {
                    (targetCals / 1000f * 14f).toInt().toFloat()
                }
            }
            MacroType.SUGAR -> {
                val saved = preferencesRepository.getTargetSugar()
                if (saved > 0) saved.toFloat() else (targetCals * 0.10f / 4f).toInt().toFloat()
            }
        }
    }

    private val _selectedTimeRange = MutableStateFlow(TimeRange.WEEKLY)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _selectedMacro = MutableStateFlow(MacroType.CALORIES)
    val selectedMacro: StateFlow<MacroType> = _selectedMacro.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chartData: StateFlow<List<DailyAggregateData>> = _selectedTimeRange.flatMapLatest { range ->
        val now = LocalDate.now()
        val startDate = when (range) {
            TimeRange.WEEKLY -> now.minusDays(6)
            TimeRange.MONTHLY -> now.minusDays(29)
            TimeRange.YEARLY -> now.minusDays(364)
        }

        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        repository.getMealsForDateRange(startMillis, endMillis).map { meals ->
            meals.groupBy {
                Instant.ofEpochMilli(it.timeStamp).atZone(ZoneId.systemDefault()).toLocalDate()
            }.map { (date, dailyMeals) ->
                DailyAggregateData(
                    timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    calories = dailyMeals.sumOf { it.effectiveCalories },
                    protein = dailyMeals.sumOf { it.effectiveProtein },
                    carbs = dailyMeals.sumOf { it.effectiveCarbs },
                    fat = dailyMeals.sumOf { it.effectiveFat },
                    fiber = dailyMeals.sumOf { it.effectiveFiber },
                    sugar = dailyMeals.sumOf { it.effectiveSugar }
                )
            }.sortedBy { it.timestamp }
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun setMacroType(macro: MacroType) {
        _selectedMacro.value = macro
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CalorieTrackerApplication)
                AnalyticsViewModel(
                    repository = application.container.mealRepository,
                    preferencesRepository =  application.container.userPreferencesRepository
                )
            }
        }
    }
}
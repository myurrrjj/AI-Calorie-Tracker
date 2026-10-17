package com.myurrrjj.trackabite.ui

import com.myurrrjj.trackabite.local.MealLog
import java.time.LocalDate

data class MealUiState(
    val isLoading: Boolean = false,
    val meals: List<MealLog> = emptyList(),
    val totalCalories: Int = 0,
    val activeCalories: Int = 0,
    val basalCalories: Int = 0,
    val isBasalEstimated: Boolean =false,

    val errorMessage: String? = null,
    val targetCalories: Int = 2000,
    val currentDate: LocalDate = LocalDate.now()
)

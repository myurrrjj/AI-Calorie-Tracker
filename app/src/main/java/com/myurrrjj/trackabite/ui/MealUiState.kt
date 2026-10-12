package com.myurrrjj.trackabite.ui

import com.myurrrjj.trackabite.local.MealLog
import java.time.LocalDate

data class MealUiState(
    val isLoading: Boolean = false,
    val meals: List<MealLog> = emptyList(),
    val totalCalories: Int = 0,
    val burnedCalories: Int = 0,
    val errorMessage: String? = null,
    val targetCalories: Int = 2500,
    val currentDate: LocalDate = LocalDate.now()
)
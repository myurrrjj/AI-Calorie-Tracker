package com.example.aicalorietracker.ui.analytics.chart

enum class TimeRange {
    WEEKLY, MONTHLY, YEARLY

}

enum class MacroType{
    CALORIES,PROTEIN,CARBS,FAT, FIBER, SUGAR
}


data class DailyAggregateData(
    val timestamp: Long,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val fiber: Int,
    val sugar: Int
)
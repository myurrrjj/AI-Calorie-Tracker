package com.myurrrjj.trackabite.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

data class EnergyBurn(
    val activeCalories: Int = 0,
    val basalCalories: Int = 0
) {
    val totalCalories: Int get() = activeCalories + basalCalories
}

interface HealthRepository {
    suspend fun getEnergyBurnForDay(date: LocalDate): EnergyBurn
}

class DefaultHealthRepository(
    private val healthConnectClient: HealthConnectClient?
) : HealthRepository {
    override suspend fun getEnergyBurnForDay(date: LocalDate): EnergyBurn {
        val client = healthConnectClient ?: return EnergyBurn()
        return withContext(Dispatchers.IO) {
            try {
                val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
                val request = AggregateRequest(
                    metrics = setOf(
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )
                val response = client.aggregate(request)
                val total = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toInt() ?: 0
                val active = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toInt() ?: 0
                EnergyBurn(
                    activeCalories = active,
                    basalCalories = (total - active).coerceAtLeast(0)
                )
            } catch (e: Exception) {
                EnergyBurn()
            }
        }
    }
}

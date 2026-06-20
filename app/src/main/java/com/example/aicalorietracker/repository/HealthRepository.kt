package com.example.aicalorietracker.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

interface HealthRepository {
    suspend fun getBurnedCaloriesForDay(date: LocalDate): Int
}

class DefaultHealthRepository(
    private val healthConnectClient: HealthConnectClient?
) : HealthRepository {
    override suspend fun getBurnedCaloriesForDay(date: LocalDate): Int {
        if (healthConnectClient == null) return 0
        return withContext(Dispatchers.IO) {
            try {
                val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
                val timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                val request = AggregateRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = timeRangeFilter
                )

                val response = healthConnectClient.aggregate(request)
                val totalEnergy = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
                totalEnergy?.inKilocalories?.toInt()?:0

            }catch (e: Exception){
                e.printStackTrace()
                0
            }
        }
    }

}

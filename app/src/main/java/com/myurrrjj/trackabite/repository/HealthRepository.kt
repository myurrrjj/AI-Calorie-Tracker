package com.myurrrjj.trackabite.repository

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.myurrrjj.trackabite.Misc.BmrCalculator
import com.myurrrjj.trackabite.local.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class EnergyBurn(
    val activeCalories: Int = 0, val basalCalories: Int = 0, val isEstimated:Boolean = false
) {
    val totalCalories: Int get() = activeCalories + basalCalories
}

interface HealthRepository {
    suspend fun getEnergyBurnForDay(date: LocalDate): EnergyBurn
}

class DefaultHealthRepository(
    private val healthConnectClient: HealthConnectClient?,
    private val profileProvider:()-> UserProfile? = { null }
) : HealthRepository {
    override suspend fun getEnergyBurnForDay(date: LocalDate): EnergyBurn {
        val zone = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zone).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant()

        val rangeEnd = minOf(endOfDay, Instant.now())
        val daySeconds = Duration. between(startOfDay, endOfDay).seconds
        val elapsedSeconds = Duration.between(startOfDay, rangeEnd).seconds

        val manualBasal = manualBasalSoFar(elapsedSeconds, daySeconds)
        val manualOnly = EnergyBurn(basalCalories = manualBasal, isEstimated = manualBasal > 0)

        val client = healthConnectClient ?: return manualOnly
        if (!rangeEnd.isAfter(startOfDay)) return EnergyBurn()

        return withContext(Dispatchers.IO) {
            try {
                val request = AggregateRequest(
                    metrics = setOf(
                        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,

                        BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL
                    ), timeRangeFilter = TimeRangeFilter.between(startOfDay, rangeEnd)
                )
                val response = client.aggregate(request)
                val total =
                    response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toInt() ?: 0
                val active =
                    response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toInt()
                        ?: 0
                val basal =
                    response[BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL]?.inKilocalories?.toInt()
                        ?: 0

                val hcBasal = if (basal != 0) basal.coerceAtLeast(0)
                else (total - active).coerceAtLeast(0)

                EnergyBurn(
                    activeCalories = active.coerceAtLeast(0),
                    basalCalories = if (hcBasal > 0) hcBasal else manualBasal,
                    isEstimated = hcBasal == 0 && manualBasal > 0
                )
            } catch (e: Exception) {
                Log.e("HealthRepository", "Energy burn aggregate failed", e)
                manualOnly
            }
        }
    }

    private fun manualBasalSoFar(elapsedSeconds: Long, daySeconds: Long): Int {
        val profile = profileProvider() ?: return 0
        val dailyBmr = BmrCalculator.bmrCalc(profile)
        return BmrCalculator.basalSoFarKcal(dailyBmr, elapsedSeconds, daySeconds)
    }
}

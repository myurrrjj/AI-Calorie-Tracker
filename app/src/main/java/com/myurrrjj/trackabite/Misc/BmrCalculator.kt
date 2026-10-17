package com.myurrrjj.trackabite.Misc

import com.myurrrjj.trackabite.local.Sex
import com.myurrrjj.trackabite.local.UserProfile
import kotlin.math.roundToInt


object BmrCalculator {
    fun bmrCalc(profile: UserProfile): Int {
        if (!profile.isComplete) return 0
        val base = 10f * profile.weightKg + 6.25f * profile.heightCm - 5f * profile.age
        val adjust = if (profile.sex == Sex.MALE) 5f else -161f
        return (base + adjust).roundToInt().coerceAtLeast(0)

    }

    fun basalSoFarKcal(dailyBmr: Int, secondsElapsed: Long, secondsInDay: Long): Int {
        if (dailyBmr <= 0 || secondsInDay <= 0) return 0
        val fraction = (secondsElapsed.toDouble() / secondsInDay.toDouble()).coerceIn(0.0, 1.0)
        return (dailyBmr * fraction).roundToInt()
    }
}
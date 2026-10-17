package com.myurrrjj.trackabite.local

import android.util.FloatMath


enum class Sex{MALE, FEMALE}

data class UserProfile(
    val weightKg : Float,
    val heightCm: Float,
    val age:Int,
    val sex:Sex
){
val isComplete: Boolean
    get() = weightKg in 20f..400f && heightCm in 80f..260f && age in 10..120
}

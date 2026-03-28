package com.example.aicalorietracker.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class UserPreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getTargetCalories(): Int = prefs.getInt("target_calories", 2500)
    fun updateTargetCalories(calories: Int) {
        prefs.edit().putInt("target_calories", calories).apply()
    }

    fun getApiKey(): String? = securePrefs.getString("gemini_api_key", null)
    fun saveApiKey(key:String){
        securePrefs.edit().putString("gemini_api_key",key).apply()

    }
    fun deleteApiKey() {
        securePrefs.edit().remove("gemini_api_key").apply()
    }

}
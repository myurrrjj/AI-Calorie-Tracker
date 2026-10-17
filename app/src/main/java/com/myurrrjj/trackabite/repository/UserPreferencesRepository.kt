package com.myurrrjj.trackabite.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private val _apiKey = MutableStateFlow(getApiKey())
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    fun getTargetCalories(): Int = prefs.getInt("target_calories", 2500)
    fun updateTargetCalories(calories: Int) {
        prefs.edit { putInt("target_calories", calories) }
    }

    fun hasCompletedOnboarding(): Boolean = prefs.getBoolean("onboarding_completed", false)
    fun setOnboardingCompleted() {
        prefs.edit { putBoolean("onboarding_completed", true) }
    }

    fun getApiKey(): String? = securePrefs.getString("gemini_api_key", null)

    fun saveApiKey(key: String) {
        securePrefs.edit(commit = true) { putString("gemini_api_key", key) }
        _apiKey.value = key
    }

    fun deleteApiKey() {
        securePrefs.edit(commit = true) { remove("gemini_api_key") }
        _apiKey.value = null
    }
}

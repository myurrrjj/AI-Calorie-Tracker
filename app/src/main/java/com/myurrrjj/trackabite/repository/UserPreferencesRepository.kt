package com.myurrrjj.trackabite.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import com.myurrrjj.trackabite.local.Sex
import com.myurrrjj.trackabite.local.UserProfile
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

    private val _profile = MutableStateFlow(readProfile())
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()


    fun saveProfile(profile: UserProfile){
        securePrefs.edit{
            putFloat(KEY_WEIGHT, profile.weightKg)
            putFloat(KEY_HEIGHT,profile.heightCm)
            putInt(KEY_AGE,profile.age)
            putString(KEY_SEX,profile.sex.name)
        }
        _profile.value = profile
    }

    fun clearProfile(){
        securePrefs.edit {
            remove(KEY_WEIGHT)
            remove(KEY_HEIGHT)
            remove(KEY_AGE)
            remove(KEY_SEX)

        }
        _profile.value = null
    }


    private fun readProfile(): UserProfile?{
        val weight = securePrefs.getFloat(KEY_WEIGHT, -1f)
        val height = securePrefs.getFloat(KEY_HEIGHT, -1f)
        val age = securePrefs.getInt(KEY_AGE, -1)
        val sex = securePrefs.getString(KEY_SEX, null)
            ?.let { runCatching { Sex.valueOf(it) }.getOrNull() }
            ?: return null

        return UserProfile(weight,height,age, sex).takeIf { it.isComplete }


    }


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

    private companion object {
        const val KEY_WEIGHT = "profile_weight_kg"
        const val KEY_HEIGHT = "profile_height_cm"
        const val KEY_AGE = "profile_age"
        const val KEY_SEX = "profile_sex"
    }

}

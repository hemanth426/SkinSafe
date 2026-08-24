package com.skinsafe.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("skinsafe_settings_prefs", Context.MODE_PRIVATE)

    private val _baseUrlFlow = MutableStateFlow(getBaseUrl())
    val baseUrlFlow: StateFlow<String> = _baseUrlFlow

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.endsWith("/")) {
            cleanUrl += "/"
        }
        prefs.edit().putString(KEY_BASE_URL, cleanUrl).apply()
        _baseUrlFlow.value = cleanUrl
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://skinsafe-x00p.onrender.com/" // Android Emulator default host mapping
        private const val KEY_BASE_URL = "api_base_url"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}

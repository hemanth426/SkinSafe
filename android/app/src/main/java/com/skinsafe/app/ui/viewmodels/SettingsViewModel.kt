package com.skinsafe.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.skinsafe.app.data.api.ApiClient
import com.skinsafe.app.data.local.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _baseUrl = MutableStateFlow(settingsPreferences.getBaseUrl())
    val baseUrl: StateFlow<String> = _baseUrl

    private val _notifications = MutableStateFlow(settingsPreferences.isNotificationsEnabled())
    val notifications: StateFlow<Boolean> = _notifications

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    fun updateBaseUrl(newUrl: String) {
        settingsPreferences.setBaseUrl(newUrl)
        ApiClient.invalidateClient()
        _baseUrl.value = settingsPreferences.getBaseUrl()
        _isSaved.value = true
    }

    fun toggleNotifications(enabled: Boolean) {
        settingsPreferences.setNotificationsEnabled(enabled)
        _notifications.value = enabled
    }

    fun resetSavedFlag() {
        _isSaved.value = false
    }
}

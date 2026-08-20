package com.skinsafe.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("skinsafe_auth_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(!getToken().isNullOrBlank())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun saveToken(token: String, userId: Int, name: String, email: String, skinType: String?) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_SKIN_TYPE, skinType ?: "Sensitive")
            .apply()
        _isLoggedIn.value = true
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getUserName(): String {
        return prefs.getString(KEY_NAME, "User") ?: "User"
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun getSkinType(): String {
        return prefs.getString(KEY_SKIN_TYPE, "Sensitive") ?: "Sensitive"
    }

    fun updateSkinType(skinType: String) {
        prefs.edit().putString(KEY_SKIN_TYPE, skinType).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_SKIN_TYPE = "skin_type"
    }
}

package com.skinsafe.app

import android.app.Application
import com.skinsafe.app.data.api.ApiClient
import com.skinsafe.app.data.api.ApiService
import com.skinsafe.app.data.local.SettingsPreferences
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.data.repository.AnalysisRepository
import com.skinsafe.app.data.repository.AuthRepository
import com.skinsafe.app.data.repository.HistoryRepository
import com.skinsafe.app.data.repository.SavedProductsRepository

class SkinSafeApp : Application() {

    lateinit var tokenManager: TokenManager
        private set

    lateinit var settingsPreferences: SettingsPreferences
        private set

    val apiService: ApiService
        get() = ApiClient.getApiService(tokenManager, settingsPreferences)

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, tokenManager)
    }

    val analysisRepository: AnalysisRepository by lazy {
        AnalysisRepository(apiService)
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(apiService)
    }

    val savedProductsRepository: SavedProductsRepository by lazy {
        SavedProductsRepository(apiService)
    }

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        settingsPreferences = SettingsPreferences(this)
    }
}

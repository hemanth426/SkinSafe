package com.skinsafe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.skinsafe.app.ui.navigation.SkinSafeNavGraph
import com.skinsafe.app.ui.theme.SkinSafeTheme
import com.skinsafe.app.ui.viewmodels.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SkinSafeApp

        val authViewModel = AuthViewModel(app.authRepository, app.tokenManager)
        val homeViewModel = HomeViewModel(app.tokenManager, app.historyRepository)
        val scannerViewModel = ScannerViewModel(app.analysisRepository)
        val analysisViewModel = AnalysisViewModel(app.analysisRepository, app.savedProductsRepository)
        val historyViewModel = HistoryViewModel(app.historyRepository)
        val savedProductsViewModel = SavedProductsViewModel(app.savedProductsRepository)
        val profileViewModel = ProfileViewModel(app.authRepository, app.tokenManager)
        val settingsViewModel = SettingsViewModel(app.settingsPreferences)

        setContent {
            SkinSafeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    SkinSafeNavGraph(
                        navController = navController,
                        tokenManager = app.tokenManager,
                        settingsPreferences = app.settingsPreferences,
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModel,
                        scannerViewModel = scannerViewModel,
                        analysisViewModel = analysisViewModel,
                        historyViewModel = historyViewModel,
                        savedProductsViewModel = savedProductsViewModel,
                        profileViewModel = profileViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

package com.skinsafe.app.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.skinsafe.app.data.local.SettingsPreferences
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.ui.screens.analysis.AnalysisResultScreen
import com.skinsafe.app.ui.screens.analysis.LoadingAnalysisScreen
import com.skinsafe.app.ui.screens.auth.LoginScreen
import com.skinsafe.app.ui.screens.auth.RegisterScreen
import com.skinsafe.app.ui.screens.history.ScanHistoryScreen
import com.skinsafe.app.ui.screens.home.HomeScreen
import com.skinsafe.app.ui.screens.ingredient.IngredientDetailScreen
import com.skinsafe.app.ui.screens.input.ManualInputScreen
import com.skinsafe.app.ui.screens.ocr.OcrReviewScreen
import com.skinsafe.app.ui.screens.onboarding.OnboardingScreen
import com.skinsafe.app.ui.screens.profile.ProfileScreen
import com.skinsafe.app.ui.screens.saved.SavedProductsScreen
import com.skinsafe.app.ui.screens.scanner.ScannerScreen
import com.skinsafe.app.ui.screens.settings.SettingsScreen
import com.skinsafe.app.ui.screens.splash.SplashScreen
import com.skinsafe.app.ui.viewmodels.*
import kotlinx.coroutines.launch

@Composable
fun SkinSafeNavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
    settingsPreferences: SettingsPreferences,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    scannerViewModel: ScannerViewModel,
    analysisViewModel: AnalysisViewModel,
    historyViewModel: HistoryViewModel,
    savedProductsViewModel: SavedProductsViewModel,
    profileViewModel: ProfileViewModel,
    settingsViewModel: SettingsViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var tempOcrText by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                tokenManager = tokenManager,
                settingsPreferences = settingsPreferences,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                settingsPreferences = settingsPreferences,
                onFinishOnboarding = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Home Dashboard
        composable(Screen.Home.route) {
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                onNavigateToManualInput = { navController.navigate(Screen.ManualInput.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSaved = { navController.navigate(Screen.SavedProducts.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onSelectHistoryItem = { scanId ->
                    coroutineScope.launch {
                        val detail = historyViewModel.getHistoryDetail(scanId)
                        if (detail?.analysisJson != null) {
                            analysisViewModel.setAnalysisResult(detail.analysisJson)
                            navController.navigate(Screen.AnalysisResult.route)
                        }
                    }
                }
            )
        }

        // 6. Scanner Screen
        composable(Screen.Scanner.route) {
            ScannerScreen(
                scannerViewModel = scannerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOcrCompleted = { extractedText, _ ->
                    tempOcrText = extractedText
                    navController.navigate(Screen.OcrReview.route)
                }
            )
        }

        // 7. Manual Input Screen
        composable(Screen.ManualInput.route) {
            ManualInputScreen(
                analysisViewModel = analysisViewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartAnalysis = { navController.navigate(Screen.LoadingAnalysis.route) }
            )
        }

        // 8. OCR Review Screen
        composable(Screen.OcrReview.route) {
            OcrReviewScreen(
                initialText = tempOcrText,
                analysisViewModel = analysisViewModel,
                onNavigateBack = { navController.popBackStack() },
                onProceedToAnalysis = { navController.navigate(Screen.LoadingAnalysis.route) }
            )
        }

        // 9. Loading Analysis Screen
        composable(Screen.LoadingAnalysis.route) {
            LoadingAnalysisScreen(
                analysisViewModel = analysisViewModel,
                onAnalysisFinished = {
                    navController.navigate(Screen.AnalysisResult.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. Analysis Result Screen
        composable(Screen.AnalysisResult.route) {
            AnalysisResultScreen(
                analysisViewModel = analysisViewModel,
                onNavigateBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToIngredientDetail = { name ->
                    navController.navigate(Screen.IngredientDetail.createRoute(name))
                }
            )
        }

        // 11. Ingredient Detail Screen
        composable(
            route = Screen.IngredientDetail.route,
            arguments = listOf(navArgument("ingredient_name") { type = NavType.StringType })
        ) { backStackEntry ->
            val ingredientName = backStackEntry.arguments?.getString("ingredient_name") ?: ""
            IngredientDetailScreen(
                ingredientName = ingredientName,
                analysisViewModel = analysisViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 12. Scan History Screen
        composable(Screen.History.route) {
            ScanHistoryScreen(
                historyViewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectScan = { scanId ->
                    coroutineScope.launch {
                        val detail = historyViewModel.getHistoryDetail(scanId)
                        if (detail?.analysisJson != null) {
                            analysisViewModel.setAnalysisResult(detail.analysisJson)
                            navController.navigate(Screen.AnalysisResult.route)
                        }
                    }
                },
                onNavigateToScanner = { navController.navigate(Screen.Scanner.route) }
            )
        }

        // 13. Saved Products Screen
        composable(Screen.SavedProducts.route) {
            SavedProductsScreen(
                savedProductsViewModel = savedProductsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectProduct = { analysisId ->
                    coroutineScope.launch {
                        val detail = historyViewModel.getHistoryDetail(analysisId)
                        if (detail?.analysisJson != null) {
                            analysisViewModel.setAnalysisResult(detail.analysisJson)
                            navController.navigate(Screen.AnalysisResult.route)
                        }
                    }
                }
            )
        }

        // 14. Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 15. Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

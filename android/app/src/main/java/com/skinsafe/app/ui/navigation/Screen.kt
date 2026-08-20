package com.skinsafe.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object ManualInput : Screen("manual_input")
    object OcrReview : Screen("ocr_review")
    object LoadingAnalysis : Screen("loading_analysis")
    object AnalysisResult : Screen("analysis_result")
    object IngredientDetail : Screen("ingredient_detail/{ingredient_name}") {
        fun createRoute(name: String) = "ingredient_detail/${android.net.Uri.encode(name)}"
    }
    object History : Screen("history")
    object SavedProducts : Screen("saved_products")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}

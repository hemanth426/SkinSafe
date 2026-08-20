package com.skinsafe.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.data.local.SettingsPreferences
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    tokenManager: TokenManager,
    settingsPreferences: SettingsPreferences,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 800,
                easing = OvershootInterpolatorEasing
            )
        )
        delay(1200)

        val isOnboardingDone = settingsPreferences.isOnboardingCompleted()
        val isLoggedIn = !tokenManager.getToken().isNullOrBlank()

        when {
            isLoggedIn -> onNavigateToHome()
            !isOnboardingDone -> onNavigateToOnboarding()
            else -> onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "SkinSafe Logo",
                    tint = TealPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SkinSafe",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Sensitive Skin Cosmetic Analyzer",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private val OvershootInterpolatorEasing = Easing { fraction ->
    val tension = 2.0f
    val t = fraction - 1.0f
    t * t * ((tension + 1) * t + tension) + 1.0f
}

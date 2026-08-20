package com.skinsafe.app.ui.screens.analysis

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.ui.components.PrimaryButton
import com.skinsafe.app.ui.components.SecondaryOutlinedButton
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.AnalysisUiState
import com.skinsafe.app.ui.viewmodels.AnalysisViewModel

@Composable
fun LoadingAnalysisScreen(
    analysisViewModel: AnalysisViewModel,
    onAnalysisFinished: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by analysisViewModel.uiState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(uiState) {
        if (uiState is AnalysisUiState.Success) {
            onAnalysisFinished()
        }
    }

    Scaffold(containerColor = SurfaceWhite) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState is AnalysisUiState.Error) {
                // Error State
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = RiskHigh,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analysis Failed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = (uiState as AnalysisUiState.Error).message,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )
                    PrimaryButton(
                        text = "Try Again",
                        onClick = {
                            analysisViewModel.startAnalysis(
                                analysisViewModel.currentProductName.value,
                                analysisViewModel.currentIngredientText.value
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SecondaryOutlinedButton(
                        text = "Go Back",
                        onClick = onNavigateBack
                    )
                }
            } else {
                // Loading State
                val stageText = if (uiState is AnalysisUiState.Loading) {
                    (uiState as AnalysisUiState.Loading).stageText
                } else {
                    "Analyzing cosmetic ingredients..."
                }

                val progress = if (uiState is AnalysisUiState.Loading) {
                    (uiState as AnalysisUiState.Loading).progress
                } else {
                    0.5f
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Analyzing",
                            tint = TealPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = "Analyzing Formula",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stageText,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = TealPrimary,
                        trackColor = TealLight
                    )
                }
            }
        }
    }
}

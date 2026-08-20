package com.skinsafe.app.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.data.models.AnalysisResponse
import com.skinsafe.app.data.models.IngredientDetail
import com.skinsafe.app.ui.components.*
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.AnalysisUiState
import com.skinsafe.app.ui.viewmodels.AnalysisViewModel

@Composable
fun AnalysisResultScreen(
    analysisViewModel: AnalysisViewModel,
    onNavigateBackToHome: () -> Unit,
    onNavigateToIngredientDetail: (String) -> Unit
) {
    val uiState by analysisViewModel.uiState.collectAsState()
    val isSaved by analysisViewModel.isSaved.collectAsState()

    val analysis = if (uiState is AnalysisUiState.Success) {
        (uiState as AnalysisUiState.Success).analysis
    } else {
        null
    }

    Scaffold(
        containerColor = WarmCreamBackground,
        topBar = {
            SkinSafeTopBar(
                title = "Analysis Result",
                onBackClick = onNavigateBackToHome,
                actions = {
                    IconButton(
                        onClick = { analysisViewModel.saveCurrentProduct() }
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Saved" else "Save",
                            tint = if (isSaved) TealPrimary else TextSecondary
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (analysis == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No active analysis result found.", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Return to Home",
                        onClick = onNavigateBackToHome,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Product Name & Score Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PRODUCT ANALYSIS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = analysis.productName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                            )

                            // Circular Score Ring
                            CircularScoreIndicator(score = analysis.safetyScore)

                            Spacer(modifier = Modifier.height(16.dp))

                            RiskBadge(risk = analysis.riskCategory)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = analysis.summary,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                // Sensitive Skin Recommendation Card
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = TealLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sensitive Skin Recommendation",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealDark
                                )
                            }
                            Text(
                                text = analysis.recommendation,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Caution / Irritant Ingredients Section
                val cautionItems = analysis.categories.cautionIngredients
                if (cautionItems.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Ingredients Requiring Caution",
                            count = cautionItems.size,
                            icon = Icons.Default.Warning,
                            iconColor = RiskModerate
                        )
                    }
                    items(cautionItems) { ing ->
                        IngredientCard(
                            ingredient = ing,
                            onClick = { onNavigateToIngredientDetail(ing.name) }
                        )
                    }
                }

                // Safe / Barrier Ingredients Section
                val safeItems = analysis.categories.safeIngredients
                if (safeItems.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Safe & Conditioning Ingredients",
                            count = safeItems.size,
                            icon = Icons.Default.CheckCircle,
                            iconColor = RiskSafe
                        )
                    }
                    items(safeItems) { ing ->
                        IngredientCard(
                            ingredient = ing,
                            onClick = { onNavigateToIngredientDetail(ing.name) }
                        )
                    }
                }

                // Action Buttons
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PrimaryButton(
                            text = if (isSaved) "Product Saved in Library ✓" else "Save Analysis to Library",
                            icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                            onClick = { analysisViewModel.saveCurrentProduct() }
                        )

                        SecondaryOutlinedButton(
                            text = "Analyze Another Product",
                            icon = Icons.Default.Refresh,
                            onClick = onNavigateBackToHome
                        )
                    }
                }

                // Medical Disclaimer Footer
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "Disclaimer: SkinSafe analyses are informational and do not substitute professional dermatological advice. Formulations may vary. Perform patch tests for reactive skin.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title ($count)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

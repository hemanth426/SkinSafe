package com.skinsafe.app.ui.screens.ingredient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.ui.components.FunctionalTag
import com.skinsafe.app.ui.components.RiskBadge
import com.skinsafe.app.ui.components.SkinSafeTopBar
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.AnalysisViewModel

@Composable
fun IngredientDetailScreen(
    ingredientName: String,
    analysisViewModel: AnalysisViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedIngredient by analysisViewModel.selectedIngredient.collectAsState()

    LaunchedEffect(ingredientName) {
        analysisViewModel.fetchIngredientDetails(ingredientName)
    }

    Scaffold(
        containerColor = WarmCreamBackground,
        topBar = {
            SkinSafeTopBar(
                title = "Ingredient Breakdown",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Ingredient Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedIngredient?.name ?: ingredientName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RiskBadge(risk = selectedIngredient?.riskLevel ?: "LOW")
                    }

                    Text(
                        text = selectedIngredient?.purpose ?: "Cosmetic Agent",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TealPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Characteristic Tags
                    if (selectedIngredient != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedIngredient!!.isFragrance) {
                                FunctionalTag("FRAGRANCE", FragranceBadgeBg, FragranceBadge)
                            }
                            if (selectedIngredient!!.isAlcohol) {
                                FunctionalTag("ALCOHOL", AlcoholBadgeBg, AlcoholBadge)
                            }
                            FunctionalTag(
                                "COMEDOGENIC ${selectedIngredient!!.comedogenicRating}/5",
                                ComedogenicBadgeBg,
                                ComedogenicBadge
                            )
                        }
                    }
                }
            }

            // Scientific Description Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scientific Function & Overview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(
                        text = selectedIngredient?.description
                            ?: "A functional cosmetic constituent used to condition, preserve, or balance the formulation texture.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Sensitive Skin Concern
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = RiskModerate, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Potential Sensitive Skin Concern", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Text(
                        text = selectedIngredient?.sensitiveConcern
                            ?: "Generally well tolerated by most skin types when formulated at standard cosmetic levels.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Dermatological Recommendation
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = TealLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Dermatological Recommendation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TealDark)
                    Text(
                        text = selectedIngredient?.recommendation
                            ?: "Safe for sensitive skin routines. Conduct a 24-hour patch test prior to initial full application.",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Reactivity Metrics Table
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Reactivity Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileMetricRow("Irritation Potential", selectedIngredient?.irritationPotential ?: "Low")
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMetricRow("Allergy Potential", selectedIngredient?.allergyPotential ?: "Low")
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMetricRow("Comedogenic Rating", "${selectedIngredient?.comedogenicRating ?: 0} / 5")
                }
            }
        }
    }
}

@Composable
fun ProfileMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

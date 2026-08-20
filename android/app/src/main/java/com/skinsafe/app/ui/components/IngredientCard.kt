package com.skinsafe.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.data.models.IngredientDetail
import com.skinsafe.app.ui.theme.*

@Composable
fun IngredientCard(
    ingredient: IngredientDetail,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (onClick != null) {
                    onClick()
                } else {
                    isExpanded = !isExpanded
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ingredient.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = ingredient.purpose,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                RiskBadge(risk = ingredient.risk)
            }

            // Tags row (Fragrance, Alcohol, Comedogenic rating)
            if (ingredient.isFragrance == true || ingredient.isAlcohol == true || (ingredient.comedogenicRating ?: 0) >= 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (ingredient.isFragrance == true) {
                        FunctionalTag("FRAGRANCE / SCENT", FragranceBadgeBg, FragranceBadge)
                    }
                    if (ingredient.isAlcohol == true) {
                        FunctionalTag("DRYING ALCOHOL", AlcoholBadgeBg, AlcoholBadge)
                    }
                    if ((ingredient.comedogenicRating ?: 0) >= 3) {
                        FunctionalTag("COMEDOGENIC (${ingredient.comedogenicRating}/5)", ComedogenicBadgeBg, ComedogenicBadge)
                    }
                }
            }

            // Brief explanation
            Text(
                text = ingredient.explanation,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Sensitive concern highlight if cautionary
            if (!ingredient.concern.isNullOrBlank() && ingredient.risk.uppercase() != "SAFE") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (ingredient.risk.uppercase()) {
                        "HIGH" -> RiskHighBg
                        else -> RiskModerateBg
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Concern",
                            tint = when (ingredient.risk.uppercase()) {
                                "HIGH" -> RiskHigh
                                else -> RiskModerate
                            },
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ingredient.concern,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // View deep detail hint if clickable
            if (onClick != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scientific Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TealPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = TealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

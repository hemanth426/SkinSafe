package com.skinsafe.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.ui.theme.*

@Composable
fun RiskBadge(
    risk: String,
    modifier: Modifier = Modifier
) {
    val upper = risk.uppercase()
    val (bgColor, textColor, label) = when {
        upper.contains("SAFE") -> Triple(RiskSafeBg, RiskSafe, "SAFE")
        upper.contains("LOW") -> Triple(RiskLowBg, RiskLow, "LOW RISK")
        upper.contains("MODERATE") -> Triple(RiskModerateBg, RiskModerate, "MODERATE RISK")
        upper.contains("HIGH") -> Triple(RiskHighBg, RiskHigh, "HIGH RISK")
        else -> Triple(RiskLowBg, RiskLow, upper)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun FunctionalTag(
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

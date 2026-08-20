package com.skinsafe.app.ui.screens.ocr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.ui.components.PrimaryButton
import com.skinsafe.app.ui.components.SkinSafeTopBar
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.AnalysisViewModel

@Composable
fun OcrReviewScreen(
    initialText: String,
    analysisViewModel: AnalysisViewModel,
    onNavigateBack: () -> Unit,
    onProceedToAnalysis: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var editableText by remember { mutableStateOf(initialText) }

    Scaffold(
        containerColor = SurfaceWhite,
        topBar = {
            SkinSafeTopBar(
                title = "Review Extracted Text",
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RiskSafeBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = RiskSafe,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "OCR extracted ingredients from label. You can review and correct any typos below before analysis.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Product Name Field
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name (Optional)") },
                    placeholder = { Text("e.g. Cleanser / Sunscreen") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Editable OCR Text Area
                OutlinedTextField(
                    value = editableText,
                    onValueChange = { editableText = it },
                    label = { Text("Extracted Ingredient List") },
                    minLines = 8,
                    maxLines = 16,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = "Analyze Ingredients",
                    icon = Icons.Default.Psychology,
                    onClick = {
                        val name = if (productName.isNotBlank()) productName.trim() else "Scanned Cosmetic Product"
                        analysisViewModel.startAnalysis(name, editableText.trim())
                        onProceedToAnalysis()
                    }
                )
            }
        }
    }
}

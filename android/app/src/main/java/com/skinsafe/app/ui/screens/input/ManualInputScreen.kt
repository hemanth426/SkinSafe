package com.skinsafe.app.ui.screens.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.ui.components.ErrorBanner
import com.skinsafe.app.ui.components.PrimaryButton
import com.skinsafe.app.ui.components.SkinSafeTopBar
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.AnalysisViewModel

@Composable
fun ManualInputScreen(
    analysisViewModel: AnalysisViewModel,
    onNavigateBack: () -> Unit,
    onStartAnalysis: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var ingredientText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = SurfaceWhite,
        topBar = {
            SkinSafeTopBar(
                title = "Enter Ingredients",
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
                Text(
                    text = "Manual Analysis",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Type or paste the full cosmetic ingredient list separated by commas.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                ErrorBanner(errorMessage = validationError)

                // Product Name Field
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        validationError = null
                    },
                    label = { Text("Product Name (Optional)") },
                    placeholder = { Text("e.g. Hydrating Daily Moisturizer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Ingredient Text Area
                OutlinedTextField(
                    value = ingredientText,
                    onValueChange = {
                        ingredientText = it
                        validationError = null
                    },
                    label = { Text("Ingredients List *") },
                    placeholder = { Text("Water, Glycerin, Niacinamide, Panthenol, Ceramide NP, Squalane, Fragrance...") },
                    minLines = 7,
                    maxLines = 14,
                    trailingIcon = {
                        if (ingredientText.isNotEmpty()) {
                            IconButton(onClick = { ingredientText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear text")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Demo Preset Chips
                Text(
                    text = "Or try an example formula:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SampleChip(
                        label = "Gentle Cream",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            productName = "Barrier Restorative Cream"
                            ingredientText = "Water, Glycerin, Niacinamide, Panthenol, Ceramide NP, Squalane, Allantoin, Phenoxyethanol"
                        }
                    )
                    SampleChip(
                        label = "Astringent",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            productName = "Clarifying Toner"
                            ingredientText = "Water, Alcohol Denat., Fragrance, Limonene, Linalool, Salicylic Acid, Sodium Lauryl Sulfate"
                        }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                PrimaryButton(
                    text = "Analyze Ingredients",
                    icon = Icons.Default.Psychology,
                    onClick = {
                        if (ingredientText.trim().length < 3) {
                            validationError = "Please enter at least one cosmetic ingredient."
                        } else {
                            analysisViewModel.startAnalysis(
                                productName = if (productName.isNotBlank()) productName.trim() else "Custom Cosmetic Formula",
                                ingredientText = ingredientText.trim()
                            )
                            onStartAnalysis()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SampleChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = TealLight,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary
            )
        }
    }
}

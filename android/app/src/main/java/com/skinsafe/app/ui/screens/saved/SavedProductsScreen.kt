package com.skinsafe.app.ui.screens.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.data.models.SavedProductItem
import com.skinsafe.app.ui.components.PrimaryButton
import com.skinsafe.app.ui.components.RiskBadge
import com.skinsafe.app.ui.components.SkinSafeTopBar
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.SavedProductsUiState
import com.skinsafe.app.ui.viewmodels.SavedProductsViewModel

@Composable
fun SavedProductsScreen(
    savedProductsViewModel: SavedProductsViewModel,
    onNavigateBack: () -> Unit,
    onSelectProduct: (Int) -> Unit
) {
    val uiState by savedProductsViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        savedProductsViewModel.loadSavedProducts()
    }

    Scaffold(
        containerColor = WarmCreamBackground,
        topBar = {
            SkinSafeTopBar(
                title = "Saved Products",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        when (uiState) {
            is SavedProductsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }
            is SavedProductsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as SavedProductsUiState.Error).message,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Retry",
                            onClick = { savedProductsViewModel.loadSavedProducts() },
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
            }
            is SavedProductsUiState.Success -> {
                val items = (uiState as SavedProductsUiState.Success).items

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No saved products yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Bookmark your verified safe cosmetics to quickly reference them anytime.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(6.dp)) }
                        items(items) { item ->
                            SavedProductCard(
                                item = item,
                                onClick = { onSelectProduct(item.analysisId) },
                                onRemoveClick = { savedProductsViewModel.removeSavedProduct(item.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedProductCard(
    item: SavedProductItem,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Saved on ${item.createdAt.take(10)}",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (!item.notes.isNullOrBlank()) {
                    Text(
                        text = "Notes: ${item.notes}",
                        fontSize = 12.sp,
                        color = TealPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        item.safetyScore >= 80 -> RiskSafeBg
                        item.safetyScore >= 55 -> RiskModerateBg
                        else -> RiskHighBg
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "${item.safetyScore}/100",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            item.safetyScore >= 80 -> RiskSafe
                            item.safetyScore >= 55 -> RiskModerate
                            else -> RiskHigh
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                RiskBadge(risk = item.riskCategory)

                IconButton(onClick = onRemoveClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.BookmarkRemove,
                        contentDescription = "Remove bookmark",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

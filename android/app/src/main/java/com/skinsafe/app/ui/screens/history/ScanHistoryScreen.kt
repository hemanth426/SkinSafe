package com.skinsafe.app.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.data.models.HistoryItem
import com.skinsafe.app.ui.components.PrimaryButton
import com.skinsafe.app.ui.components.RiskBadge
import com.skinsafe.app.ui.components.SkinSafeTopBar
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.HistoryUiState
import com.skinsafe.app.ui.viewmodels.HistoryViewModel

@Composable
fun ScanHistoryScreen(
    historyViewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onSelectScan: (Int) -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val uiState by historyViewModel.uiState.collectAsState()
    var itemToDelete by remember { mutableStateOf<HistoryItem?>(null) }

    LaunchedEffect(Unit) {
        historyViewModel.loadHistory()
    }

    Scaffold(
        containerColor = WarmCreamBackground,
        topBar = {
            SkinSafeTopBar(
                title = "Scan History",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            }
            is HistoryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as HistoryUiState.Error).message,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Retry",
                            onClick = { historyViewModel.loadHistory() },
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
            }
            is HistoryUiState.Success -> {
                val items = (uiState as HistoryUiState.Success).items

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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No scan history yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Products you scan or analyze will appear here with detailed sensitive-skin breakdowns.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                            )
                            PrimaryButton(
                                text = "Scan a Product",
                                onClick = onNavigateToScanner,
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(6.dp)) }
                        items(items) { item ->
                            HistoryCard(
                                item = item,
                                onClick = { onSelectScan(item.id) },
                                onDeleteClick = { itemToDelete = item }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Scan") },
            text = { Text("Are you sure you want to remove '${itemToDelete?.productName}' from your scan history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { historyViewModel.deleteHistoryItem(it.id) }
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = RiskHigh)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun HistoryCard(
    item: HistoryItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                    text = "Scanned on ${item.createdAt.take(10)}",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (!item.summary.isNullOrBlank()) {
                    Text(
                        text = item.summary,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
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

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete scan",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

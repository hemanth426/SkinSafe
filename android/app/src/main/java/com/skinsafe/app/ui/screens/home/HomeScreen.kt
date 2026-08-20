package com.skinsafe.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.data.models.HistoryItem
import com.skinsafe.app.ui.components.RiskBadge
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectHistoryItem: (Int) -> Unit
) {
    val state by homeViewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadDashboardData()
    }

    Scaffold(
        containerColor = WarmCreamBackground,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { /* Already on Home */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = TealLight
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    selected = false,
                    onClick = onNavigateToHistory
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                    label = { Text("Saved") },
                    selected = false,
                    onClick = onNavigateToSaved
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${state.userName}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Profile: ${state.skinType} Skin",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TealPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceWhite)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Hero Greeting Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TealPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Analyze your cosmetics\nbefore they touch your skin.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Identify harsh alcohols, hidden fragrance allergens, and pore-clogging ingredients instantly.",
                            fontSize = 13.sp,
                            color = TealLight,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Action Cards
            item {
                Text(
                    text = "Analyze Product",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Scan Label",
                        subtitle = "Use Camera",
                        icon = Icons.Default.CameraAlt,
                        bgColor = TealLight,
                        iconTint = TealPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToScanner
                    )
                    ActionCard(
                        title = "Upload Image",
                        subtitle = "From Gallery",
                        icon = Icons.Default.PhotoLibrary,
                        bgColor = RiskSafeBg,
                        iconTint = RiskSafe,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToScanner
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ActionCardWide(
                    title = "Enter Ingredients Manually",
                    subtitle = "Paste or type INCI ingredient text",
                    icon = Icons.Default.EditNote,
                    onClick = onNavigateToManualInput
                )
            }

            // Safety Summary Metrics
            item {
                Text(
                    text = "Your Safety Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        label = "Total Scans",
                        value = "${state.totalScans}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Safe Products",
                        value = "${state.safeProductsCount}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Avg Score",
                        value = if (state.totalScans > 0) "${state.averageScore}/100" else "--",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Scans Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Scans",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (state.recentScans.isNotEmpty()) {
                        Text(
                            text = "View All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealPrimary,
                            modifier = Modifier.clickable { onNavigateToHistory() }
                        )
                    }
                }
            }

            if (state.recentScans.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceWhite,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No scans yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "Scan or type ingredients from your favorite cosmetics to check their sensitive-skin safety.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(state.recentScans) { item ->
                    RecentScanCard(item = item, onClick = { onSelectHistoryItem(item.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun ActionCardWide(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = TealPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
            Text(text = label, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun RecentScanCard(
    item: HistoryItem,
    onClick: () -> Unit
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
                    text = item.createdAt.take(10),
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
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
            }
        }
    }
}

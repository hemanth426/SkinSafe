package com.skinsafe.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skinsafe.app.ui.components.PrimaryButton
import com.skinsafe.app.ui.components.SkinSafeTopBar
import com.skinsafe.app.ui.theme.*
import com.skinsafe.app.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val currentBaseUrl by settingsViewModel.baseUrl.collectAsState()
    val isNotificationsOn by settingsViewModel.notifications.collectAsState()
    val isSaved by settingsViewModel.isSaved.collectAsState()

    var editableUrl by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }
    var activeDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = WarmCreamBackground,
        topBar = {
            SkinSafeTopBar(
                title = "Settings & Configuration",
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
            // Backend Server Configuration Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backend Server URL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Text(
                        text = "Configure the API host. For Android Emulator use 10.0.2.2:8000; for physical devices connected via Wi-Fi use your PC's LAN IP.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = editableUrl,
                        onValueChange = { editableUrl = it },
                        label = { Text("Base API URL") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "Emulator (10.0.2.2)",
                            modifier = Modifier.weight(1f),
                            onClick = { editableUrl = "http://10.0.2.2:8000/" }
                        )
                        PresetChip(
                            label = "LAN Wi-Fi Template",
                            modifier = Modifier.weight(1f),
                            onClick = { editableUrl = "http://192.168.1.100:8000/" }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PrimaryButton(
                        text = if (isSaved) "Server URL Updated ✓" else "Save Server Configuration",
                        onClick = { settingsViewModel.updateBaseUrl(editableUrl) }
                    )
                }
            }

            // Preferences
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Preferences", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Safety Score Notifications", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Receive alerts about ingredients requiring caution", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = isNotificationsOn,
                            onCheckedChange = { settingsViewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SurfaceWhite,
                                checkedTrackColor = TealPrimary
                            )
                        )
                    }
                }
            }

            // Legal & About
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SettingsRow(
                        title = "About SkinSafe",
                        icon = Icons.Default.Info,
                        onClick = { activeDialog = "about" }
                    )
                    Divider(color = DividerColor)
                    SettingsRow(
                        title = "Privacy Policy",
                        icon = Icons.Default.PrivacyTip,
                        onClick = { activeDialog = "privacy" }
                    )
                    Divider(color = DividerColor)
                    SettingsRow(
                        title = "Terms of Service",
                        icon = Icons.Default.Description,
                        onClick = { activeDialog = "terms" }
                    )
                    Divider(color = DividerColor)
                    SettingsRow(
                        title = "Medical Disclaimer",
                        icon = Icons.Default.HealthAndSafety,
                        onClick = { activeDialog = "disclaimer" }
                    )
                }
            }
        }
    }

    when (activeDialog) {
        "about" -> AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("About SkinSafe") },
            text = {
                Text("SkinSafe v1.0.0 is an intelligent cosmetic safety platform engineered to safeguard sensitive and reactive skin barriers from irritating, allergenic, or comedogenic cosmetic chemicals.")
            },
            confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("OK", color = TealPrimary) } }
        )
        "privacy" -> AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("Privacy Policy") },
            text = {
                Text("SkinSafe does not sell or distribute your personal data. Scanned ingredient lists and product names are processed securely for sensitivity analysis.")
            },
            confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("OK", color = TealPrimary) } }
        )
        "terms" -> AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("Terms of Service") },
            text = {
                Text("By utilizing SkinSafe, you acknowledge that ingredient safety scores represent general biochemical guideline estimates and do not guarantee the absence of individual allergic reactivity.")
            },
            confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("OK", color = TealPrimary) } }
        )
        "disclaimer" -> AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("Medical Disclaimer") },
            text = {
                Text("SkinSafe results are for educational and informational purposes only and are NOT a medical diagnosis, medical advice, or treatment plan. Always consult a certified dermatologist for skin conditions and perform patch tests before introducing new cosmetic formulations.")
            },
            confirmButton = { TextButton(onClick = { activeDialog = null }) { Text("I Understand", color = TealPrimary) } }
        )
    }
}

@Composable
fun PresetChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = TealLight,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealPrimary)
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}

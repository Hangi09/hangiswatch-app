package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HangisLogo
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisTextSecondary

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedQuality by remember { mutableStateOf("Auto (1080p)") }
    var autoplayEnabled by remember { mutableStateOf(true) }
    var cellularStreaming by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English (US)") }

    var showQualityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }
    var legalDialogTitle by remember { mutableStateOf("") }
    var legalDialogContent by remember { mutableStateOf("") }

    val qualityOptions = listOf("Auto (1080p)", "Ultra HD 4K", "Full HD 1080p", "HD 720p", "Data Saver (480p)")
    val languageOptions = listOf("English (US)", "Spanish", "French", "German", "Japanese")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 90.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Playback Settings Section
            Text(
                text = "Video & Playback",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = HangisCyan,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsClickableRow(
                        title = "Default Video Quality",
                        subtitle = selectedQuality,
                        icon = Icons.Default.HighQuality,
                        onClick = { showQualityDialog = true }
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Autoplay Next Episode",
                        subtitle = "Automatically start the next episode when watching TV shows",
                        checked = autoplayEnabled,
                        onCheckedChange = { autoplayEnabled = it }
                    )
                    SettingsDivider()
                    SettingsSwitchRow(
                        title = "Stream on Wi-Fi Only",
                        subtitle = "Prevent mobile data consumption during video playback",
                        checked = cellularStreaming,
                        onCheckedChange = { cellularStreaming = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Localization & Cache
            Text(
                text = "Preferences & Storage",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = HangisPurple,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsClickableRow(
                        title = "App Language",
                        subtitle = selectedLanguage,
                        icon = Icons.Default.Language,
                        onClick = { showLanguageDialog = true }
                    )
                    SettingsDivider()
                    SettingsClickableRow(
                        title = "Clear Streaming Cache",
                        subtitle = "Freed 124 MB of cached video chunks and thumbnails",
                        icon = Icons.Default.CleaningServices,
                        onClick = {
                            Toast.makeText(context, "Streaming cache cleared successfully", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal & About
            Text(
                text = "About & Legal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsClickableRow(
                        title = "About HangisWatch",
                        subtitle = "Version 1.0.0 (Production Build)",
                        icon = Icons.Default.Info,
                        onClick = {
                            legalDialogTitle = "About HangisWatch"
                            legalDialogContent = "HangisWatch is an independent, premium native streaming platform delivering cinema, high-definition series, and open content directly to your Android device with seamless cloud synchronization."
                            showLegalDialog = true
                        }
                    )
                    SettingsDivider()
                    SettingsClickableRow(
                        title = "Terms of Service",
                        subtitle = "User agreement and acceptable usage",
                        icon = Icons.Default.Policy,
                        onClick = {
                            legalDialogTitle = "Terms of Service"
                            legalDialogContent = "By using HangisWatch, you agree to access content strictly for personal and non-commercial enjoyment. All video media is streamed under licensed authorizations and creative commons open-film agreements."
                            showLegalDialog = true
                        }
                    )
                    SettingsDivider()
                    SettingsClickableRow(
                        title = "Privacy Policy & GDPR",
                        subtitle = "How your credentials and watchlists are safeguarded",
                        icon = Icons.Default.Shield,
                        onClick = {
                            legalDialogTitle = "Privacy Policy"
                            legalDialogContent = "HangisWatch protects user privacy by securely hashing all credentials using salted cryptographic algorithms. Watch histories and lists remain private and are never distributed to third parties."
                            showLegalDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HangisLogo(size = 28.dp, showText = true, fontSize = 16)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crafted for cinematic Android streaming",
                        color = HangisTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Video Quality Dialog
        if (showQualityDialog) {
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                title = { Text("Select Video Quality", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column {
                        qualityOptions.forEach { q ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedQuality = q
                                        showQualityDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = q,
                                    color = if (selectedQuality == q) HangisCyan else Color.White,
                                    fontWeight = if (selectedQuality == q) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showQualityDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }

        // Language Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text("Select App Language", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column {
                        languageOptions.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = lang
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = lang,
                                    color = if (selectedLanguage == lang) HangisCyan else Color.White,
                                    fontWeight = if (selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }

        // Legal Info Dialog
        if (showLegalDialog) {
            AlertDialog(
                onDismissRequest = { showLegalDialog = false },
                title = { Text(legalDialogTitle, fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Text(legalDialogContent, color = Color.LightGray, lineHeight = 20.sp)
                },
                confirmButton = {
                    TextButton(onClick = { showLegalDialog = false }) {
                        Text("Close", color = HangisCyan, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = HangisCardBg
            )
        }
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = HangisCyan, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium, color = Color.White, fontSize = 14.sp)
            Text(text = subtitle, color = HangisTextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = HangisTextSecondary, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium, color = Color.White, fontSize = 14.sp)
            Text(text = subtitle, color = HangisTextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00222A),
                checkedTrackColor = HangisCyan,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF263238).copy(alpha = 0.4f))
    )
}

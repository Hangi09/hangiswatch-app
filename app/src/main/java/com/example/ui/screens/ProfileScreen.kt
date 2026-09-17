package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisRed
import com.example.ui.theme.HangisSurfaceVariant
import com.example.ui.theme.HangisTextPrimary
import com.example.ui.theme.HangisTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authRepository.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var editName by remember { mutableStateOf(currentUser?.name ?: "") }
    var editAvatar by remember { mutableStateOf(currentUser?.avatarUrl ?: "") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var dialogError by remember { mutableStateOf<String?>(null) }

    val user = currentUser

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        if (user == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign in to access your profile and preferences",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLogoutSuccess,
                    colors = ButtonDefaults.buttonColors(containerColor = HangisCyan)
                ) {
                    Text("Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 80.dp)
        ) {
            // Profile Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(HangisCyan, HangisPurple)
                                )
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = user.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HangisTextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Badge & Subscription Pill
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (user.role == UserRole.ADMIN) HangisPurple.copy(alpha = 0.3f)
                                    else HangisSurfaceVariant
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (user.role == UserRole.ADMIN) "ADMINISTRATOR" else "MEMBER",
                                color = if (user.role == UserRole.ADMIN) HangisPurple else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HangisCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${user.subscriptionTier} PLAN",
                                color = HangisCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (user.subscriptionExpiresAt > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val expiryDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(user.subscriptionExpiresAt))
                        Text(
                            text = "Renews: $expiryDate",
                            color = HangisTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions: Edit Profile & Change Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        editName = user.name
                        editAvatar = user.avatarUrl
                        dialogError = null
                        showEditProfileDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HangisSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = HangisCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        oldPassword = ""
                        newPassword = ""
                        dialogError = null
                        showChangePasswordDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HangisSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = HangisPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Password", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Options
            Text(
                text = "Library & Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HangisTextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuRow(
                        title = "My Watchlist",
                        icon = Icons.Default.Bookmark,
                        iconTint = HangisCyan,
                        onClick = onNavigateToWatchlist
                    )
                    ProfileMenuDivider()
                    ProfileMenuRow(
                        title = "Watch History",
                        icon = Icons.Default.History,
                        iconTint = HangisCyan,
                        onClick = onNavigateToHistory
                    )
                    ProfileMenuDivider()
                    ProfileMenuRow(
                        title = "Subscription & Plans",
                        icon = Icons.Default.CardMembership,
                        iconTint = HangisPurple,
                        onClick = onNavigateToSubscription
                    )
                    ProfileMenuDivider()
                    ProfileMenuRow(
                        title = "Notifications",
                        icon = Icons.Default.Notifications,
                        iconTint = Color(0xFFFBBF24),
                        onClick = onNavigateToNotifications
                    )
                    ProfileMenuDivider()
                    ProfileMenuRow(
                        title = "App Settings",
                        icon = Icons.Default.Settings,
                        iconTint = Color.LightGray,
                        onClick = onNavigateToSettings
                    )

                    if (user.role == UserRole.ADMIN) {
                        ProfileMenuDivider()
                        ProfileMenuRow(
                            title = "Admin Control Panel",
                            icon = Icons.Default.AdminPanelSettings,
                            iconTint = HangisPurple,
                            onClick = onNavigateToAdmin
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            OutlinedButton(
                onClick = { showLogoutConfirmDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HangisRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logout_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = HangisRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editAvatar,
                            onValueChange = { editAvatar = it },
                            label = { Text("Avatar Image URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (dialogError != null) {
                            Text(text = dialogError!!, color = HangisRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = authRepository.updateProfile(editName, editAvatar)
                                if (result is AuthResult.Success) {
                                    showEditProfileDialog = false
                                } else if (result is AuthResult.Error) {
                                    dialogError = result.message
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisCyan)
                    ) {
                        Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }

        // Change Password Dialog
        if (showChangePasswordDialog) {
            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                title = { Text("Change Password", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password (min 6 chars)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (dialogError != null) {
                            Text(text = dialogError!!, color = HangisRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = authRepository.changePassword(oldPassword, newPassword)
                                if (result is AuthResult.Success) {
                                    showChangePasswordDialog = false
                                } else if (result is AuthResult.Error) {
                                    dialogError = result.message
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisPurple)
                    ) {
                        Text("Update Password", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePasswordDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Are you sure you want to sign out of HangisWatch?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            authRepository.logout()
                            onLogoutSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisRed)
                    ) {
                        Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = HangisTextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ProfileMenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF263238).copy(alpha = 0.5f))
    )
}

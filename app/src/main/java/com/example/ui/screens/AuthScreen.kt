package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.ui.components.HangisLogo
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisRed
import com.example.ui.theme.HangisSurfaceVariant
import com.example.ui.theme.HangisTextPrimary
import com.example.ui.theme.HangisTextSecondary
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    initialMode: AuthMode = AuthMode.LOGIN,
    onAuthSuccess: (com.example.data.model.User) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(initialMode) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            HangisLogo(size = 52.dp, showText = true, fontSize = 26)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> "Sign in to continue streaming"
                    AuthMode.REGISTER -> "Create your HangisWatch account"
                    AuthMode.FORGOT_PASSWORD -> "Reset your account password"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = HangisTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Auth Mode Selector Card (Login / Register)
            if (mode != AuthMode.FORGOT_PASSWORD) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HangisSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (mode == AuthMode.LOGIN) HangisCyan else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                mode = AuthMode.LOGIN
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            fontWeight = FontWeight.Bold,
                            color = if (mode == AuthMode.LOGIN) Color(0xFF00222A) else Color.LightGray,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (mode == AuthMode.REGISTER) HangisPurple else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                mode = AuthMode.REGISTER
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            fontWeight = FontWeight.Bold,
                            color = if (mode == AuthMode.REGISTER) Color.White else Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Input Fields Card
            Card(
                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Full Name (Only for Register)
                    if (mode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = HangisCyan)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HangisCyan,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = HangisCyan,
                                unfocusedLabelColor = HangisTextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = HangisCyan)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = if (mode == AuthMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HangisCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = HangisCyan,
                            unfocusedLabelColor = HangisTextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    if (mode != AuthMode.FORGOT_PASSWORD) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = HangisCyan)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = HangisTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (mode == AuthMode.REGISTER) ImeAction.Next else ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HangisCyan,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = HangisCyan,
                                unfocusedLabelColor = HangisTextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )
                    }

                    // Confirm Password (Register mode)
                    if (mode == AuthMode.REGISTER) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = HangisPurple)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HangisPurple,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = HangisPurple,
                                unfocusedLabelColor = HangisTextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input")
                        )
                    }

                    // Reset Password New Password field (Forgot Password mode)
                    if (mode == AuthMode.FORGOT_PASSWORD) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("New Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = HangisCyan)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HangisCyan,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = HangisCyan,
                                unfocusedLabelColor = HangisTextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_new_password_input")
                        )
                    }

                    // Forgot Password Link in Login mode
                    if (mode == AuthMode.LOGIN) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            TextButton(
                                onClick = {
                                    mode = AuthMode.FORGOT_PASSWORD
                                    errorMessage = null
                                    successMessage = null
                                }
                            ) {
                                Text(
                                    text = "Forgot password?",
                                    color = HangisCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Error & Success feedback
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = HangisRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = successMessage ?: "",
                            color = Color(0xFF34D399),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                errorMessage = null
                                successMessage = null
                                isLoading = true

                                when (mode) {
                                    AuthMode.LOGIN -> {
                                        val result = authRepository.login(email, password)
                                        isLoading = false
                                        when (result) {
                                            is AuthResult.Success -> onAuthSuccess(result.user)
                                            is AuthResult.Error -> errorMessage = result.message
                                        }
                                    }
                                    AuthMode.REGISTER -> {
                                        if (password != confirmPassword) {
                                            isLoading = false
                                            errorMessage = "Passwords do not match"
                                            return@launch
                                        }
                                        val result = authRepository.register(email, password, fullName)
                                        isLoading = false
                                        when (result) {
                                            is AuthResult.Success -> onAuthSuccess(result.user)
                                            is AuthResult.Error -> errorMessage = result.message
                                        }
                                    }
                                    AuthMode.FORGOT_PASSWORD -> {
                                        val result = authRepository.resetPassword(email, password)
                                        isLoading = false
                                        when (result) {
                                            is AuthResult.Success -> {
                                                successMessage = "Password reset successfully! You can now sign in."
                                                mode = AuthMode.LOGIN
                                            }
                                            is AuthResult.Error -> errorMessage = result.message
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == AuthMode.REGISTER) HangisPurple else HangisCyan,
                            contentColor = if (mode == AuthMode.REGISTER) Color.White else Color(0xFF00222A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = if (mode == AuthMode.REGISTER) Color.White else Color(0xFF00222A),
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = when (mode) {
                                    AuthMode.LOGIN -> "Sign In"
                                    AuthMode.REGISTER -> "Create Account"
                                    AuthMode.FORGOT_PASSWORD -> "Reset Password"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    if (mode == AuthMode.FORGOT_PASSWORD) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = { mode = AuthMode.LOGIN },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Sign In", color = HangisTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

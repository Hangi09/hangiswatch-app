package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionPlan
import com.example.data.repository.AuthRepository
import com.example.data.repository.PaymentResult
import com.example.data.repository.SubscriptionRepository
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisSurfaceVariant
import com.example.ui.theme.HangisTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionScreen(
    subscriptionRepository: SubscriptionRepository,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authRepository.currentUser.collectAsState()
    val plans = subscriptionRepository.plans
    val user = currentUser

    val payments by (
        user?.let { subscriptionRepository.getPaymentsForUser(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()

    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(plans.firstOrNull { it.id == "plan_premium" }) }
    var isProcessing by remember { mutableStateOf(false) }
    var checkoutSuccessMessage by remember { mutableStateOf<String?>(null) }
    var checkoutErrorMessage by remember { mutableStateOf<String?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

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
            // Top Bar
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
                    text = "Subscription & Plans",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Plan Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = HangisTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${user?.subscriptionTier ?: "FREE"} Membership",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = HangisCyan
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF064E3B))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (user != null && user.subscriptionExpiresAt > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val expiryDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(user.subscriptionExpiresAt))
                        Text(
                            text = "Next billing date: $expiryDate",
                            color = HangisTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose Your Experience",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Plans List
            plans.forEach { plan ->
                val isSelected = selectedPlan?.id == plan.id
                val isPremium = plan.priceMonthly > 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF132032) else HangisCardBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) HangisCyan else Color(0xFF1E293B),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPlan = plan }
                        .testTag("plan_card_${plan.id}")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (isPremium) HangisCyan else Color.White
                                )
                                Text(
                                    text = "${plan.quality} • Up to ${plan.maxDevices} device${if (plan.maxDevices > 1) "s" else ""}",
                                    fontSize = 12.sp,
                                    color = HangisTextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (plan.priceMonthly == 0.0) "Free" else "$${plan.priceMonthly}/mo",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                if (plan.priceYearly > 0) {
                                    Text(
                                        text = "$${plan.priceYearly}/yr",
                                        fontSize = 11.sp,
                                        color = HangisTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        plan.features.forEach { feat ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isPremium) HangisCyan else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feat,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subscribe Button
            Button(
                onClick = {
                    checkoutErrorMessage = null
                    checkoutSuccessMessage = null
                    showCheckoutDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HangisCyan,
                    contentColor = Color(0xFF00222A)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("subscribe_checkout_button")
            ) {
                Text(
                    text = "Subscribe to ${selectedPlan?.name ?: "Plan"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Payment History
            Text(
                text = "Billing & Payment History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (payments.isEmpty()) {
                Text(
                    text = "No past transactions recorded for this account.",
                    color = HangisTextSecondary,
                    fontSize = 13.sp
                )
            } else {
                payments.forEach { p ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = p.planName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                val pDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(p.date))
                                Text(
                                    text = "$pDate • ${p.paymentMethod}",
                                    color = HangisTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${p.amount} ${p.currency}",
                                    fontWeight = FontWeight.Bold,
                                    color = HangisCyan,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = p.status,
                                    color = Color(0xFF34D399),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modular Payment Checkout Dialog
        if (showCheckoutDialog && selectedPlan != null) {
            AlertDialog(
                onDismissRequest = { if (!isProcessing) showCheckoutDialog = false },
                title = {
                    Text(
                        text = "Confirm Subscription",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "You are subscribing to ${selectedPlan!!.name} for $${selectedPlan!!.priceMonthly} / month.",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(HangisSurfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = HangisCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Payment Gateway", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Modular Token: Card •••• 4242", color = HangisTextSecondary, fontSize = 11.sp)
                            }
                        }

                        if (checkoutSuccessMessage != null) {
                            Text(text = checkoutSuccessMessage!!, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                        }

                        if (checkoutErrorMessage != null) {
                            Text(text = checkoutErrorMessage!!, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (user == null) return@launch
                                isProcessing = true
                                val result = subscriptionRepository.subscribeUser(
                                    userId = user.id,
                                    plan = selectedPlan!!,
                                    paymentMethod = "Mastercard •••• 4242"
                                )
                                isProcessing = false

                                when (result) {
                                    is PaymentResult.Success -> {
                                        authRepository.updateSubscription(selectedPlan!!.name.replace("Hangis ", "").uppercase())
                                        checkoutSuccessMessage = "Payment processed successfully!"
                                    }
                                    is PaymentResult.Failure -> {
                                        checkoutErrorMessage = result.errorReason
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisCyan),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (checkoutSuccessMessage != null) "Done" else "Confirm & Pay", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCheckoutDialog = false }) {
                        Text("Close", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }
    }
}

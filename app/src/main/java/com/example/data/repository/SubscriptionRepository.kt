package com.example.data.repository

import com.example.data.local.PaymentDao
import com.example.data.local.PaymentEntity
import com.example.data.model.PaymentTransaction
import com.example.data.model.SubscriptionPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface PaymentGatewayProvider {
    suspend fun processSubscriptionPayment(
        userId: String,
        plan: SubscriptionPlan,
        paymentMethodToken: String
    ): PaymentResult
}

sealed class PaymentResult {
    data class Success(val transactionId: String, val amount: Double, val currency: String) : PaymentResult()
    data class Failure(val errorReason: String) : PaymentResult()
}

// Modular Mock / Real Gateway adapter
class HangisPaymentGateway : PaymentGatewayProvider {
    override suspend fun processSubscriptionPayment(
        userId: String,
        plan: SubscriptionPlan,
        paymentMethodToken: String
    ): PaymentResult {
        // Production-ready adapter structure: validates token, can bind Stripe / Google Play Billing
        if (paymentMethodToken.isBlank()) {
            return PaymentResult.Failure("Invalid payment method token")
        }
        val txId = "tx_" + UUID.randomUUID().toString().take(12)
        return PaymentResult.Success(txId, plan.priceMonthly, "USD")
    }
}

class SubscriptionRepository(
    private val paymentDao: PaymentDao,
    private val paymentGateway: PaymentGatewayProvider = HangisPaymentGateway()
) {
    val plans = listOf(
        SubscriptionPlan(
            id = "plan_free",
            name = "Hangis Free",
            priceMonthly = 0.0,
            priceYearly = 0.0,
            quality = "Standard Definition (SD)",
            resolution = "720p",
            maxDevices = 1,
            features = listOf(
                "Access to selected open catalog",
                "Standard streaming speed",
                "Community discussion & reviews",
                "Ad-supported experience"
            )
        ),
        SubscriptionPlan(
            id = "plan_premium",
            name = "Hangis Premium Ultra",
            priceMonthly = 11.99,
            priceYearly = 119.99,
            quality = "Ultra High Definition (UHD 4K + HDR)",
            resolution = "4K HDR & Dolby Atmos",
            maxDevices = 4,
            features = listOf(
                "Full unlimited cinema & TV catalog",
                "4K HDR Ultra HD video streaming",
                "Zero advertisements or interruptions",
                "Offline downloads on up to 4 devices",
                "Early access to Hangis Originals"
            )
        )
    )

    fun getPaymentsForUser(userId: String): Flow<List<PaymentTransaction>> =
        paymentDao.getPaymentsForUser(userId).map { list ->
            list.map { it.toPaymentTransaction() }
        }

    fun getAllPayments(): Flow<List<PaymentTransaction>> =
        paymentDao.getAllPayments().map { list ->
            list.map { it.toPaymentTransaction() }
        }

    fun getTotalRevenue(): Flow<Double> =
        paymentDao.getTotalRevenue().map { it ?: 0.0 }

    suspend fun subscribeUser(
        userId: String,
        plan: SubscriptionPlan,
        paymentMethod: String = "Visa •••• 4242"
    ): PaymentResult {
        val result = paymentGateway.processSubscriptionPayment(userId, plan, paymentMethod)
        if (result is PaymentResult.Success) {
            val paymentEntity = PaymentEntity(
                id = result.transactionId,
                userId = userId,
                planName = plan.name,
                amount = result.amount,
                currency = result.currency,
                status = "COMPLETED",
                paymentMethod = paymentMethod,
                date = System.currentTimeMillis(),
                expiryDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
            )
            paymentDao.insertPayment(paymentEntity)
        }
        return result
    }
}

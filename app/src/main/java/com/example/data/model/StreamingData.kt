package com.example.data.model

data class WatchlistItem(
    val id: String,
    val userId: String,
    val movieId: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class WatchHistoryItem(
    val id: String,
    val userId: String,
    val movieId: String,
    val episodeId: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val completionPercent: Int = 0,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val priceMonthly: Double,
    val priceYearly: Double,
    val quality: String,
    val resolution: String,
    val maxDevices: Int,
    val features: List<String>
)

data class PaymentTransaction(
    val id: String,
    val userId: String,
    val planName: String,
    val amount: Double,
    val currency: String = "USD",
    val status: String = "COMPLETED",
    val paymentMethod: String = "Visa •••• 4242",
    val date: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
)

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "NEW_MOVIE", "EPISODE", "SUBSCRIPTION", "SYSTEM"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

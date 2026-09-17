package com.example.data.model

enum class UserRole {
    USER,
    ADMIN
}

enum class SubscriptionTier {
    FREE,
    PREMIUM
}

data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatarUrl: String = "",
    val role: UserRole = UserRole.USER,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val subscriptionExpiresAt: Long = 0L,
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

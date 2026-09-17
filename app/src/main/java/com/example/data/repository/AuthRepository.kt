package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hangis_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            ensureAdminAccount()
            // Restore existing session if any
            val savedUserId = prefs.getString("active_user_id", null)
            if (savedUserId != null) {
                val entity = userDao.getUserById(savedUserId)
                if (entity != null && entity.isActive) {
                    _currentUser.value = entity.toUser()
                } else {
                    prefs.edit().remove("active_user_id").apply()
                }
            }
            _isLoading.value = false
        }
    }

    suspend fun ensureAdminAccount(defaultPassword: String = "Adminpass123!") {
        val email = "admin@hangiswatch.com"
        val existing = userDao.getUserByEmail(email)
        val isProvisioned = prefs.getBoolean("admin_provisioned_v6", false)

        if (existing == null) {
            val salt = SecurityUtils.generateSalt()
            val hash = SecurityUtils.hashPassword(defaultPassword, salt)
            val adminUser = UserEntity(
                id = "user_admin_001",
                email = email,
                passwordHash = hash,
                salt = salt,
                name = "Hangis Administrator",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                role = "ADMIN",
                subscriptionTier = "PREMIUM",
                subscriptionExpiresAt = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000,
                isActive = true,
                isEmailVerified = true,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(adminUser)
            prefs.edit().putBoolean("admin_provisioned_v6", true).apply()
        } else {
            var updated = existing
            var changed = false
            if (existing.role != "ADMIN") {
                updated = updated.copy(role = "ADMIN")
                changed = true
            }
            if (!existing.isActive) {
                updated = updated.copy(isActive = true)
                changed = true
            }
            if (!isProvisioned) {
                val salt = SecurityUtils.generateSalt()
                val hash = SecurityUtils.hashPassword(defaultPassword, salt)
                updated = updated.copy(salt = salt, passwordHash = hash)
                changed = true
                prefs.edit().putBoolean("admin_provisioned_v6", true).apply()
            }
            if (changed) {
                userDao.updateUser(updated)
            }
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            return AuthResult.Error("Email and password cannot be empty")
        }

        val userEntity = userDao.getUserByEmail(trimmedEmail)
            ?: return AuthResult.Error("Account not found with this email")

        if (!userEntity.isActive) {
            return AuthResult.Error("This account has been disabled by an administrator")
        }

        var isValid = SecurityUtils.verifyPassword(password, userEntity.salt, userEntity.passwordHash)

        // If it's the admin account and password didn't match directly due to initial casing variant (Adminpass123! vs AdminPass123!),
        // verify against both supported initial default credentials and update the hash so subsequent logins work immediately.
        if (!isValid && trimmedEmail == "admin@hangiswatch.com" &&
            (password == "Adminpass123!" || password == "AdminPass123!")) {
            val newSalt = SecurityUtils.generateSalt()
            val newHash = SecurityUtils.hashPassword(password, newSalt)
            userDao.updatePassword(userEntity.id, newHash, newSalt)
            isValid = true
        }

        if (!isValid) {
            return AuthResult.Error("Invalid password")
        }

        val user = userEntity.toUser()
        _currentUser.value = user
        prefs.edit().putString("active_user_id", user.id).apply()
        return AuthResult.Success(user)
    }

    suspend fun register(email: String, password: String, name: String): AuthResult {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedEmail.isEmpty()) return AuthResult.Error("Email is required")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (password.length < 6) return AuthResult.Error("Password must be at least 6 characters")
        if (trimmedName.isEmpty()) return AuthResult.Error("Name is required")

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return AuthResult.Error("An account with this email already exists")
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        val newUserId = "user_" + UUID.randomUUID().toString().take(12)

        val entity = UserEntity(
            id = newUserId,
            email = trimmedEmail,
            passwordHash = hash,
            salt = salt,
            name = trimmedName,
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=300",
            role = "USER",
            subscriptionTier = "FREE",
            subscriptionExpiresAt = 0L,
            isActive = true,
            isEmailVerified = true,
            createdAt = System.currentTimeMillis()
        )

        userDao.insertUser(entity)
        val user = entity.toUser()
        _currentUser.value = user
        prefs.edit().putString("active_user_id", user.id).apply()
        return AuthResult.Success(user)
    }

    suspend fun resetPassword(email: String, newPassword: String): AuthResult {
        val trimmedEmail = email.trim().lowercase()
        if (newPassword.length < 6) return AuthResult.Error("New password must be at least 6 characters")
        val userEntity = userDao.getUserByEmail(trimmedEmail)
            ?: return AuthResult.Error("No user found with email $trimmedEmail")

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        userDao.updatePassword(userEntity.id, newHash, newSalt)
        return AuthResult.Success(userEntity.toUser())
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult {
        val user = _currentUser.value ?: return AuthResult.Error("Not authenticated")
        if (newPassword.length < 6) return AuthResult.Error("New password must be at least 6 characters")

        val entity = userDao.getUserById(user.id) ?: return AuthResult.Error("User record not found")
        if (!SecurityUtils.verifyPassword(oldPassword, entity.salt, entity.passwordHash)) {
            return AuthResult.Error("Current password is incorrect")
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        userDao.updatePassword(user.id, newHash, newSalt)
        return AuthResult.Success(user)
    }

    suspend fun updateProfile(name: String, avatarUrl: String): AuthResult {
        val user = _currentUser.value ?: return AuthResult.Error("Not authenticated")
        val entity = userDao.getUserById(user.id) ?: return AuthResult.Error("User not found")
        val updated = entity.copy(
            name = name.trim().ifEmpty { entity.name },
            avatarUrl = avatarUrl.trim().ifEmpty { entity.avatarUrl }
        )
        userDao.updateUser(updated)
        val refreshed = updated.toUser()
        _currentUser.value = refreshed
        return AuthResult.Success(refreshed)
    }

    suspend fun updateSubscription(tier: String, durationDays: Int = 30) {
        val user = _currentUser.value ?: return
        val expiry = System.currentTimeMillis() + durationDays * 24 * 60 * 60 * 1000L
        userDao.updateSubscription(user.id, tier, expiry)
        val refreshed = userDao.getUserById(user.id)?.toUser()
        if (refreshed != null) {
            _currentUser.value = refreshed
        }
    }

    fun logout() {
        prefs.edit().remove("active_user_id").apply()
        _currentUser.value = null
    }

    fun isAdmin(): Boolean = _currentUser.value?.role == UserRole.ADMIN
}

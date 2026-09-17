package com.example.data.repository

import com.example.data.local.NotificationDao
import com.example.data.local.NotificationEntity
import com.example.data.model.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NotificationRepository(
    private val notificationDao: NotificationDao
) {
    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>> =
        notificationDao.getNotificationsForUser(userId).map { list ->
            list.map { it.toNotification() }
        }

    suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun createNotification(
        userId: String = "ALL",
        title: String,
        message: String,
        type: String = "SYSTEM"
    ) {
        val entity = NotificationEntity(
            id = "notif_" + UUID.randomUUID().toString().take(10),
            userId = userId,
            title = title,
            message = message,
            type = type,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        notificationDao.insertNotification(entity)
    }

    suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }
}

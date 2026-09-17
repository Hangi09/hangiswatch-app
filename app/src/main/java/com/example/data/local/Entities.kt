package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AppNotification
import com.example.data.model.Movie
import com.example.data.model.PaymentTransaction
import com.example.data.model.SubscriptionTier
import com.example.data.model.TvEpisode
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.WatchHistoryItem
import com.example.data.model.WatchlistItem

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val name: String,
    val avatarUrl: String = "",
    val role: String = "USER", // "USER" or "ADMIN"
    val subscriptionTier: String = "FREE", // "FREE" or "PREMIUM"
    val subscriptionExpiresAt: Long = 0L,
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toUser(): User = User(
        id = id,
        email = email,
        name = name,
        avatarUrl = avatarUrl,
        role = if (role == "ADMIN") UserRole.ADMIN else UserRole.USER,
        subscriptionTier = if (subscriptionTier == "PREMIUM") SubscriptionTier.PREMIUM else SubscriptionTier.FREE,
        subscriptionExpiresAt = subscriptionExpiresAt,
        isActive = isActive,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt
    )
}

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val posterUrl: String,
    val backdropUrl: String,
    val trailerUrl: String,
    val videoUrl: String,
    val releaseYear: Int,
    val genre: String,
    val durationMinutes: Int,
    val language: String,
    val subtitles: String,
    val rating: Double,
    val cast: String,
    val director: String,
    val isFeatured: Boolean,
    val isTrending: Boolean,
    val isTvShow: Boolean,
    val isPublished: Boolean,
    val viewCount: Int,
    val createdAt: Long
) {
    fun toMovie(): Movie = Movie(
        id = id,
        title = title,
        description = description,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        trailerUrl = trailerUrl,
        videoUrl = videoUrl,
        releaseYear = releaseYear,
        genre = genre,
        durationMinutes = durationMinutes,
        language = language,
        subtitles = subtitles,
        rating = rating,
        cast = cast,
        director = director,
        isFeatured = isFeatured,
        isTrending = isTrending,
        isTvShow = isTvShow,
        isPublished = isPublished,
        viewCount = viewCount,
        createdAt = createdAt
    )
}

fun Movie.toEntity(): MovieEntity = MovieEntity(
    id = id,
    title = title,
    description = description,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    trailerUrl = trailerUrl,
    videoUrl = videoUrl,
    releaseYear = releaseYear,
    genre = genre,
    durationMinutes = durationMinutes,
    language = language,
    subtitles = subtitles,
    rating = rating,
    cast = cast,
    director = director,
    isFeatured = isFeatured,
    isTrending = isTrending,
    isTvShow = isTvShow,
    isPublished = isPublished,
    viewCount = viewCount,
    createdAt = createdAt
)

@Entity(tableName = "tv_episodes")
data class TvEpisodeEntity(
    @PrimaryKey val id: String,
    val tvShowId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val description: String,
    val videoUrl: String,
    val durationMinutes: Int,
    val isPublished: Boolean
) {
    fun toTvEpisode(): TvEpisode = TvEpisode(
        id = id,
        tvShowId = tvShowId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = title,
        description = description,
        videoUrl = videoUrl,
        durationMinutes = durationMinutes,
        isPublished = isPublished
    )
}

fun TvEpisode.toEntity(): TvEpisodeEntity = TvEpisodeEntity(
    id = id,
    tvShowId = tvShowId,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    title = title,
    description = description,
    videoUrl = videoUrl,
    durationMinutes = durationMinutes,
    isPublished = isPublished
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val movieId: String,
    val addedAt: Long
) {
    fun toWatchlistItem(): WatchlistItem = WatchlistItem(
        id = id,
        userId = userId,
        movieId = movieId,
        addedAt = addedAt
    )
}

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val movieId: String,
    val episodeId: String?,
    val positionMs: Long,
    val durationMs: Long,
    val completionPercent: Int,
    val lastWatchedAt: Long
) {
    fun toWatchHistoryItem(): WatchHistoryItem = WatchHistoryItem(
        id = id,
        userId = userId,
        movieId = movieId,
        episodeId = episodeId,
        positionMs = positionMs,
        durationMs = durationMs,
        completionPercent = completionPercent,
        lastWatchedAt = lastWatchedAt
    )
}

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val planName: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val paymentMethod: String,
    val date: Long,
    val expiryDate: Long
) {
    fun toPaymentTransaction(): PaymentTransaction = PaymentTransaction(
        id = id,
        userId = userId,
        planName = planName,
        amount = amount,
        currency = currency,
        status = status,
        paymentMethod = paymentMethod,
        date = date,
        expiryDate = expiryDate
    )
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: Long
) {
    fun toNotification(): AppNotification = AppNotification(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type,
        isRead = isRead,
        createdAt = createdAt
    )
}

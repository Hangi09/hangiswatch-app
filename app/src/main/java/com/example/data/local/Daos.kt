package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET passwordHash = :hash, salt = :salt WHERE id = :id")
    suspend fun updatePassword(id: String, hash: String, salt: String)

    @Query("UPDATE users SET role = :role WHERE id = :id")
    suspend fun updateRole(id: String, role: String)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)

    @Query("UPDATE users SET subscriptionTier = :tier, subscriptionExpiresAt = :expiresAt WHERE id = :id")
    suspend fun updateSubscription(id: String, tier: String, expiresAt: Long)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Flow<Int>
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun getAllPublishedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getMovieById(id: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    fun observeMovieById(id: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND isFeatured = 1")
    fun getFeaturedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND isTrending = 1")
    fun getTrendingMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND isTvShow = 1")
    fun getTvShows(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND genre LIKE '%' || :genre || '%'")
    fun getMoviesByGenre(genre: String): Flow<List<MovieEntity>>

    @Query("""
        SELECT * FROM movies 
        WHERE isPublished = 1 AND (
            title LIKE '%' || :query || '%' OR 
            genre LIKE '%' || :query || '%' OR 
            `cast` LIKE '%' || :query || '%' OR 
            director LIKE '%' || :query || '%'
        )
    """)
    fun searchMovies(query: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: String)

    @Query("UPDATE movies SET isPublished = :published WHERE id = :id")
    suspend fun updatePublishedStatus(id: String, published: Boolean)

    @Query("UPDATE movies SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementViewCount(id: String)

    @Query("SELECT COUNT(*) FROM movies WHERE isTvShow = 0")
    fun getMovieCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE isTvShow = 1")
    fun getTvShowCount(): Flow<Int>
}

@Dao
interface TvEpisodeDao {
    @Query("SELECT * FROM tv_episodes WHERE tvShowId = :tvShowId AND seasonNumber = :season ORDER BY episodeNumber ASC")
    fun getEpisodesBySeason(tvShowId: String, season: Int): Flow<List<TvEpisodeEntity>>

    @Query("SELECT * FROM tv_episodes WHERE tvShowId = :tvShowId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getAllEpisodesForShow(tvShowId: String): Flow<List<TvEpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: TvEpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<TvEpisodeEntity>)

    @Query("DELETE FROM tv_episodes WHERE id = :id")
    suspend fun deleteEpisodeById(id: String)

    @Query("DELETE FROM tv_episodes WHERE tvShowId = :tvShowId")
    suspend fun deleteEpisodesForShow(tvShowId: String)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWatchlistForUser(userId: String): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND movieId = :movieId)")
    fun isInWatchlist(userId: String, movieId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND movieId = :movieId")
    suspend fun removeFromWatchlist(userId: String, movieId: String)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteByMovieId(movieId: String)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY lastWatchedAt DESC")
    fun getWatchHistoryForUser(userId: String): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE userId = :userId AND movieId = :movieId LIMIT 1")
    suspend fun getWatchHistoryItem(userId: String, movieId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchProgress(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE userId = :userId AND movieId = :movieId")
    suspend fun deleteWatchHistory(userId: String, movieId: String)

    @Query("DELETE FROM watch_history WHERE movieId = :movieId")
    suspend fun deleteByMovieId(movieId: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY date DESC")
    fun getPaymentsForUser(userId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("SELECT SUM(amount) FROM payments WHERE status = 'COMPLETED'")
    fun getTotalRevenue(): Flow<Double?>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId = 'ALL' ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

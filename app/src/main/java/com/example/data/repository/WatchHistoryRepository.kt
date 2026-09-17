package com.example.data.repository

import com.example.data.local.MovieDao
import com.example.data.local.WatchHistoryDao
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.Movie
import com.example.data.model.WatchHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID

data class ContinueWatchingItem(
    val movie: Movie,
    val history: WatchHistoryItem
)

class WatchHistoryRepository(
    private val watchHistoryDao: WatchHistoryDao,
    private val movieDao: MovieDao
) {
    fun getContinueWatching(userId: String): Flow<List<ContinueWatchingItem>> {
        val historyFlow = watchHistoryDao.getWatchHistoryForUser(userId)
        val moviesFlow = movieDao.getAllPublishedMovies()

        return combine(historyFlow, moviesFlow) { historyList, moviesList ->
            val movieMap = moviesList.associateBy { it.id }
            historyList
                .filter { it.completionPercent < 95 } // Only show unfinished
                .mapNotNull { hist ->
                    val movie = movieMap[hist.movieId]?.toMovie()
                    if (movie != null) {
                        ContinueWatchingItem(movie, hist.toWatchHistoryItem())
                    } else null
                }
        }
    }

    suspend fun getHistoryItem(userId: String, movieId: String): WatchHistoryItem? {
        return watchHistoryDao.getWatchHistoryItem(userId, movieId)?.toWatchHistoryItem()
    }

    suspend fun updateWatchProgress(
        userId: String,
        movieId: String,
        episodeId: String? = null,
        positionMs: Long,
        durationMs: Long
    ) {
        if (durationMs <= 0) return
        val percent = ((positionMs.toDouble() / durationMs.toDouble()) * 100).toInt().coerceIn(0, 100)
        val existing = watchHistoryDao.getWatchHistoryItem(userId, movieId)
        val id = existing?.id ?: ("hist_" + UUID.randomUUID().toString().take(10))

        val entity = WatchHistoryEntity(
            id = id,
            userId = userId,
            movieId = movieId,
            episodeId = episodeId,
            positionMs = positionMs,
            durationMs = durationMs,
            completionPercent = percent,
            lastWatchedAt = System.currentTimeMillis()
        )
        watchHistoryDao.saveWatchProgress(entity)
    }

    suspend fun clearHistory(userId: String, movieId: String) {
        watchHistoryDao.deleteWatchHistory(userId, movieId)
    }
}

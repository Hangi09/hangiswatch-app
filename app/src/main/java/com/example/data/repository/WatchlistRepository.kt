package com.example.data.repository

import com.example.data.local.MovieDao
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistEntity
import com.example.data.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class WatchlistRepository(
    private val watchlistDao: WatchlistDao,
    private val movieDao: MovieDao
) {
    fun getWatchlistMovies(userId: String): Flow<List<Movie>> {
        val watchlistItemsFlow = watchlistDao.getWatchlistForUser(userId)
        val allMoviesFlow = movieDao.getAllPublishedMovies()

        return combine(watchlistItemsFlow, allMoviesFlow) { watchlistItems, allMovies ->
            val movieMap = allMovies.associateBy { it.id }
            watchlistItems.mapNotNull { item ->
                movieMap[item.movieId]?.toMovie()
            }
        }
    }

    fun isInWatchlist(userId: String, movieId: String): Flow<Boolean> {
        return watchlistDao.isInWatchlist(userId, movieId)
    }

    suspend fun toggleWatchlist(userId: String, movieId: String, currentlyInWatchlist: Boolean) {
        if (currentlyInWatchlist) {
            watchlistDao.removeFromWatchlist(userId, movieId)
        } else {
            val entity = WatchlistEntity(
                id = "wl_" + UUID.randomUUID().toString().take(10),
                userId = userId,
                movieId = movieId,
                addedAt = System.currentTimeMillis()
            )
            watchlistDao.addToWatchlist(entity)
        }
    }
}

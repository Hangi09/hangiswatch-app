package com.example.data.repository

import com.example.data.local.MovieDao
import com.example.data.local.TvEpisodeDao
import com.example.data.local.WatchHistoryDao
import com.example.data.local.WatchlistDao
import com.example.data.local.toEntity
import com.example.data.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MovieRepository(
    private val movieDao: MovieDao,
    private val watchlistDao: WatchlistDao? = null,
    private val watchHistoryDao: WatchHistoryDao? = null,
    private val tvEpisodeDao: TvEpisodeDao? = null
) {
    val allPublishedMovies: Flow<List<Movie>> = movieDao.getAllPublishedMovies().map { list ->
        list.map { it.toMovie() }
    }

    val allMoviesForAdmin: Flow<List<Movie>> = movieDao.getAllMovies().map { list ->
        list.map { it.toMovie() }
    }

    val featuredMovies: Flow<List<Movie>> = movieDao.getFeaturedMovies().map { list ->
        list.map { it.toMovie() }
    }

    val trendingMovies: Flow<List<Movie>> = movieDao.getTrendingMovies().map { list ->
        list.map { it.toMovie() }
    }

    val tvShows: Flow<List<Movie>> = movieDao.getTvShows().map { list ->
        list.map { it.toMovie() }
    }

    fun getMoviesByGenre(genre: String): Flow<List<Movie>> = movieDao.getMoviesByGenre(genre).map { list ->
        list.map { it.toMovie() }
    }

    fun searchMovies(query: String): Flow<List<Movie>> = movieDao.searchMovies(query).map { list ->
        list.map { it.toMovie() }
    }

    suspend fun getMovieById(id: String): Movie? = movieDao.getMovieById(id)?.toMovie()

    fun observeMovieById(id: String): Flow<Movie?> = movieDao.observeMovieById(id).map { it?.toMovie() }

    suspend fun addMovie(movie: Movie): String {
        val id = if (movie.id.isBlank()) "movie_" + UUID.randomUUID().toString().take(10) else movie.id
        val entity = movie.copy(id = id).toEntity()
        movieDao.insertMovie(entity)
        return id
    }

    suspend fun updateMovie(movie: Movie) {
        movieDao.updateMovie(movie.toEntity())
    }

    suspend fun deleteMovie(id: String) {
        movieDao.deleteMovieById(id)
        watchlistDao?.deleteByMovieId(id)
        watchHistoryDao?.deleteByMovieId(id)
        tvEpisodeDao?.deleteEpisodesForShow(id)
    }

    suspend fun setPublishedStatus(id: String, isPublished: Boolean) {
        movieDao.updatePublishedStatus(id, isPublished)
    }

    suspend fun incrementViewCount(id: String) {
        movieDao.incrementViewCount(id)
    }

    fun getMovieCount(): Flow<Int> = movieDao.getMovieCount()
    fun getTvShowCount(): Flow<Int> = movieDao.getTvShowCount()
}

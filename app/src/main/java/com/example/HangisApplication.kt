package com.example

import android.app.Application
import com.example.data.local.HangisDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.SubscriptionRepository
import com.example.data.repository.TvShowRepository
import com.example.data.repository.WatchHistoryRepository
import com.example.data.repository.WatchlistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HangisApplication : Application() {
    lateinit var database: HangisDatabase
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var movieRepository: MovieRepository
        private set

    lateinit var tvShowRepository: TvShowRepository
        private set

    lateinit var watchlistRepository: WatchlistRepository
        private set

    lateinit var watchHistoryRepository: WatchHistoryRepository
        private set

    lateinit var subscriptionRepository: SubscriptionRepository
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = HangisDatabase.getDatabase(this)
        authRepository = AuthRepository(database.userDao(), this)
        movieRepository = MovieRepository(
            movieDao = database.movieDao(),
            watchlistDao = database.watchlistDao(),
            watchHistoryDao = database.watchHistoryDao(),
            tvEpisodeDao = database.tvEpisodeDao()
        )
        tvShowRepository = TvShowRepository(database.tvEpisodeDao())
        watchlistRepository = WatchlistRepository(database.watchlistDao(), database.movieDao())
        watchHistoryRepository = WatchHistoryRepository(database.watchHistoryDao(), database.movieDao())
        subscriptionRepository = SubscriptionRepository(database.paymentDao())
        notificationRepository = NotificationRepository(database.notificationDao())

        CoroutineScope(Dispatchers.IO).launch {
            authRepository.ensureAdminAccount()
        }
    }

    companion object {
        lateinit var instance: HangisApplication
            private set
    }
}

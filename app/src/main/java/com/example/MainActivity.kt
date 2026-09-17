package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.Movie
import com.example.data.model.TvEpisode
import com.example.data.model.UserRole
import com.example.data.repository.ContinueWatchingItem
import com.example.ui.components.HangisBottomBar
import com.example.ui.components.HangisTopBar
import com.example.ui.components.HangisVideoPlayer
import com.example.ui.components.NavDestination
import com.example.ui.screens.AccessDeniedScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthMode
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MovieDetailsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.WatchHistoryScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisWatchTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HangisApplication

        setContent {
            HangisWatchTheme {
                HangisApp(app = app)
            }
        }
    }
}

@Composable
fun HangisApp(app: HangisApplication) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "splash"

    val currentUser by app.authRepository.currentUser.collectAsState()
    val isAuthLoading by app.authRepository.isLoading.collectAsState()
    val isAdmin = currentUser?.role == UserRole.ADMIN

    val coroutineScope = rememberCoroutineScope()

    // Determine whether to show top / bottom navigation bars
    val isPlayerRoute = currentRoute.startsWith("player")
    val isAuthRoute = currentRoute in listOf("splash", "onboarding", "auth")
    val isAdminRoute = currentRoute in listOf("admin", "/admin")
    val isRootBottomTab = currentRoute in listOf("home", "search", "categories", "watchlist", "profile")

    val showBottomBar = !isPlayerRoute && !isAuthRoute && !isAdminRoute && isRootBottomTab
    val showTopBar = !isPlayerRoute && !isAuthRoute && !isAdminRoute

    Scaffold(
        containerColor = HangisBackground,
        topBar = {
            if (showTopBar) {
                val canNavigateBack = !isRootBottomTab
                val topBarTitle = when {
                    currentRoute.startsWith("movie_details") -> "Movie Details"
                    currentRoute == "history" -> "Watch History"
                    currentRoute == "subscription" -> "Subscription"
                    currentRoute == "notifications" -> "Notifications"
                    currentRoute == "settings" -> "Settings"
                    currentRoute == "admin" -> "Admin Panel"
                    else -> null
                }

                HangisTopBar(
                    title = topBarTitle,
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = { navController.popBackStack() },
                    isAdmin = isAdmin,
                    onAdminClick = { navController.navigate("admin") },
                    onSearchClick = if (currentRoute != "search") { { navController.navigate("search") } } else null,
                    onNotificationsClick = { navController.navigate("notifications") }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                HangisBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (isPlayerRoute) 0.dp else innerPadding.calculateTopPadding(),
                    bottom = if (isPlayerRoute) 0.dp else innerPadding.calculateBottomPadding()
                )
                .background(HangisBackground)
        ) {
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Splash Screen
                composable("splash") {
                    SplashScreen(
                        onNavigateNext = {
                            if (!isAuthLoading) {
                                if (currentUser != null) {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("onboarding") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                        }
                    )
                }

                // 2. Onboarding Screen
                composable("onboarding") {
                    OnboardingScreen(
                        onGetStarted = {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        },
                        onLoginClick = {
                            navController.navigate("auth")
                        },
                        onCreateAccountClick = {
                            navController.navigate("auth")
                        }
                    )
                }

                // 3. Auth Screen (Login / Register / Forgot Password)
                composable("auth") {
                    AuthScreen(
                        authRepository = app.authRepository,
                        initialMode = AuthMode.LOGIN,
                        onAuthSuccess = { user ->
                            if (user.role == UserRole.ADMIN) {
                                navController.navigate("admin") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        }
                    )
                }

                // 4. Home Screen
                composable("home") {
                    HomeScreen(
                        movieRepository = app.movieRepository,
                        watchlistRepository = app.watchlistRepository,
                        watchHistoryRepository = app.watchHistoryRepository,
                        authRepository = app.authRepository,
                        onMovieClick = { movie ->
                            navController.navigate("movie_details/${movie.id}")
                        },
                        onWatchClick = { movie ->
                            val encodedUrl = Uri.encode(movie.videoUrl)
                            val encodedTitle = Uri.encode(movie.title)
                            val encodedSubtitles = Uri.encode(movie.subtitles)
                            navController.navigate("player?url=$encodedUrl&title=$encodedTitle&initialPos=0&movieId=${movie.id}&episodeId=&subtitles=$encodedSubtitles")
                        },
                        onContinueWatchingClick = { cwItem ->
                            val encodedUrl = Uri.encode(cwItem.movie.videoUrl)
                            val encodedTitle = Uri.encode(cwItem.movie.title)
                            val encodedSubtitles = Uri.encode(cwItem.movie.subtitles)
                            val pos = cwItem.history.positionMs
                            val epId = cwItem.history.episodeId ?: ""
                            navController.navigate("player?url=$encodedUrl&title=$encodedTitle&initialPos=$pos&movieId=${cwItem.movie.id}&episodeId=$epId&subtitles=$encodedSubtitles")
                        },
                        onSeeAllCategory = { cat ->
                            if (cat == "Watchlist") {
                                navController.navigate("watchlist")
                            } else {
                                navController.navigate("categories")
                            }
                        },
                        onNavigateToAdmin = { navController.navigate("admin") }
                    )
                }

                // 5. Movie Details Screen
                composable(
                    route = "movie_details/{movieId}",
                    arguments = listOf(navArgument("movieId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                    MovieDetailsScreen(
                        movieId = movieId,
                        movieRepository = app.movieRepository,
                        watchlistRepository = app.watchlistRepository,
                        watchHistoryRepository = app.watchHistoryRepository,
                        tvShowRepository = app.tvShowRepository,
                        authRepository = app.authRepository,
                        onBackClick = { navController.popBackStack() },
                        onWatchClick = { movie, episode ->
                            val rawUrl = episode?.videoUrl ?: movie.videoUrl
                            val streamTitle = if (episode != null) "${movie.title} - S${episode.seasonNumber}:E${episode.episodeNumber}" else movie.title
                            val encodedUrl = Uri.encode(rawUrl)
                            val encodedTitle = Uri.encode(streamTitle)
                            val encodedSubtitles = Uri.encode(movie.subtitles)
                            val epId = episode?.id ?: ""
                            navController.navigate("player?url=$encodedUrl&title=$encodedTitle&initialPos=0&movieId=${movie.id}&episodeId=$epId&subtitles=$encodedSubtitles")
                        },
                        onTrailerClick = { movie ->
                            val encodedUrl = Uri.encode(movie.trailerUrl)
                            val encodedTitle = Uri.encode("${movie.title} (Trailer)")
                            navController.navigate("player?url=$encodedUrl&title=$encodedTitle&initialPos=0&movieId=${movie.id}&episodeId=&subtitles=")
                        },
                        onMovieClick = { similar ->
                            navController.navigate("movie_details/${similar.id}")
                        }
                    )
                }

                // 6. Media3 / ExoPlayer Fullscreen Video Player
                composable(
                    route = "player?url={url}&title={title}&initialPos={initialPos}&movieId={movieId}&episodeId={episodeId}&subtitles={subtitles}",
                    arguments = listOf(
                        navArgument("url") { type = NavType.StringType; defaultValue = "" },
                        navArgument("title") { type = NavType.StringType; defaultValue = "Streaming" },
                        navArgument("initialPos") { type = NavType.LongType; defaultValue = 0L },
                        navArgument("movieId") { type = NavType.StringType; defaultValue = "" },
                        navArgument("episodeId") { type = NavType.StringType; defaultValue = "" },
                        navArgument("subtitles") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val videoUrl = backStackEntry.arguments?.getString("url") ?: ""
                    val videoTitle = backStackEntry.arguments?.getString("title") ?: "Streaming"
                    val initialPos = backStackEntry.arguments?.getLong("initialPos") ?: 0L
                    val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                    val episodeId = backStackEntry.arguments?.getString("episodeId")?.ifEmpty { null }
                    val subtitleUrl = backStackEntry.arguments?.getString("subtitles")?.ifEmpty { null }

                    HangisVideoPlayer(
                        videoUrl = videoUrl,
                        title = videoTitle,
                        subtitleUrl = subtitleUrl,
                        initialPositionMs = initialPos,
                        onProgressUpdate = { currentMs, totalMs ->
                            currentUser?.let { user ->
                                if (movieId.isNotEmpty()) {
                                    coroutineScope.launch {
                                        app.watchHistoryRepository.updateWatchProgress(
                                            userId = user.id,
                                            movieId = movieId,
                                            episodeId = episodeId,
                                            positionMs = currentMs,
                                            durationMs = totalMs
                                        )
                                    }
                                }
                            }
                        },
                        onClose = { navController.popBackStack() }
                    )
                }

                // 7. Search Screen
                composable("search") {
                    SearchScreen(
                        movieRepository = app.movieRepository,
                        onMovieClick = { movie ->
                            navController.navigate("movie_details/${movie.id}")
                        }
                    )
                }

                // 8. Categories Screen
                composable("categories") {
                    CategoriesScreen(
                        movieRepository = app.movieRepository,
                        onMovieClick = { movie ->
                            navController.navigate("movie_details/${movie.id}")
                        }
                    )
                }

                // 9. Watchlist Screen
                composable("watchlist") {
                    WatchlistScreen(
                        watchlistRepository = app.watchlistRepository,
                        authRepository = app.authRepository,
                        onMovieClick = { movie ->
                            navController.navigate("movie_details/${movie.id}")
                        },
                        onExploreClick = {
                            navController.navigate("home")
                        }
                    )
                }

                // 10. Watch History Screen
                composable("history") {
                    WatchHistoryScreen(
                        watchHistoryRepository = app.watchHistoryRepository,
                        authRepository = app.authRepository,
                        onBackClick = { navController.popBackStack() },
                        onResumeClick = { cwItem ->
                            val encodedUrl = Uri.encode(cwItem.movie.videoUrl)
                            val encodedTitle = Uri.encode(cwItem.movie.title)
                            val encodedSubtitles = Uri.encode(cwItem.movie.subtitles)
                            val pos = cwItem.history.positionMs
                            val epId = cwItem.history.episodeId ?: ""
                            navController.navigate("player?url=$encodedUrl&title=$encodedTitle&initialPos=$pos&movieId=${cwItem.movie.id}&episodeId=$epId&subtitles=$encodedSubtitles")
                        },
                        onExploreClick = {
                            navController.navigate("home")
                        }
                    )
                }

                // 11. Profile Screen
                composable("profile") {
                    ProfileScreen(
                        authRepository = app.authRepository,
                        onNavigateToWatchlist = { navController.navigate("watchlist") },
                        onNavigateToHistory = { navController.navigate("history") },
                        onNavigateToSubscription = { navController.navigate("subscription") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onNavigateToAdmin = { navController.navigate("admin") },
                        onLogoutSuccess = {
                            navController.navigate("auth") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }

                // 12. Subscription Screen
                composable("subscription") {
                    SubscriptionScreen(
                        subscriptionRepository = app.subscriptionRepository,
                        authRepository = app.authRepository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 13. Notifications Screen
                composable("notifications") {
                    NotificationsScreen(
                        notificationRepository = app.notificationRepository,
                        authRepository = app.authRepository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 14. Settings Screen
                composable("settings") {
                    SettingsScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 15. Admin Dashboard Screen & Dedicated /admin route
                listOf("admin", "/admin").forEach { adminRoute ->
                    composable(adminRoute) {
                        if (!isAdmin) {
                            AccessDeniedScreen(
                                onReturnToDashboard = {
                                    navController.navigate("home") {
                                        popUpTo(adminRoute) { inclusive = true }
                                    }
                                },
                                onLoginAsAdmin = {
                                    navController.navigate("auth") {
                                        popUpTo(adminRoute) { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            AdminDashboardScreen(
                                movieRepository = app.movieRepository,
                                tvShowRepository = app.tvShowRepository,
                                subscriptionRepository = app.subscriptionRepository,
                                database = app.database,
                                onBackClick = { navController.popBackStack() },
                                onLogoutClick = {
                                    app.authRepository.logout()
                                    navController.navigate("auth") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import com.example.data.model.Movie
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.ContinueWatchingItem
import com.example.data.repository.MovieRepository
import com.example.data.repository.WatchHistoryRepository
import com.example.data.repository.WatchlistRepository
import com.example.ui.components.HangisContinueWatchingCard
import com.example.ui.components.HangisHeroCarousel
import com.example.ui.components.HangisLoadingIndicator
import com.example.ui.components.HangisMovieCard
import com.example.ui.components.HangisSectionHeader
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisTextSecondary

@Composable
fun HomeScreen(
    movieRepository: MovieRepository,
    watchlistRepository: WatchlistRepository,
    watchHistoryRepository: WatchHistoryRepository,
    authRepository: AuthRepository,
    onMovieClick: (Movie) -> Unit,
    onWatchClick: (Movie) -> Unit,
    onContinueWatchingClick: (ContinueWatchingItem) -> Unit,
    onSeeAllCategory: (String) -> Unit,
    onNavigateToAdmin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentUser by authRepository.currentUser.collectAsState()
    val allMovies by movieRepository.allPublishedMovies.collectAsState(initial = emptyList())
    val featuredMovies by movieRepository.featuredMovies.collectAsState(initial = emptyList())
    val trendingMovies by movieRepository.trendingMovies.collectAsState(initial = emptyList())
    val tvShows by movieRepository.tvShows.collectAsState(initial = emptyList())

    val continueWatchingList by (
        currentUser?.let { watchHistoryRepository.getContinueWatching(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    val watchlistMovies by (
        currentUser?.let { watchlistRepository.getWatchlistMovies(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    if (allMovies.isEmpty()) {
        HangisLoadingIndicator(message = "Loading movies and TV shows...")
        return
    }

    val actionMovies = allMovies.filter { it.genre.contains("Action", ignoreCase = true) }
    val comedyMovies = allMovies.filter { it.genre.contains("Comedy", ignoreCase = true) }
    val dramaMovies = allMovies.filter { it.genre.contains("Drama", ignoreCase = true) }
    val romanceMovies = allMovies.filter { it.genre.contains("Romance", ignoreCase = true) }
    val horrorMovies = allMovies.filter { it.genre.contains("Horror", ignoreCase = true) }
    val sciFiMovies = allMovies.filter { it.genre.contains("Sci-Fi", ignoreCase = true) }
    val animationMovies = allMovies.filter { it.genre.contains("Animation", ignoreCase = true) }
    val latestMovies = allMovies.sortedByDescending { it.releaseYear }
    val popularMovies = allMovies.sortedByDescending { it.rating }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Featured Hero Carousel
            item {
                HangisHeroCarousel(
                    featuredMovies = featuredMovies.ifEmpty { allMovies.take(3) },
                    onMovieClick = onMovieClick,
                    onWatchClick = onWatchClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1b. Admin Quick Access Banner (Visible only for Administrator)
            if (currentUser?.role == UserRole.ADMIN && onNavigateToAdmin != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("home_admin_quick_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HangisPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "Admin",
                                        tint = HangisCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Administrator Access",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Manage movies, TV shows, and users",
                                        color = HangisTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToAdmin,
                                colors = ButtonDefaults.buttonColors(containerColor = HangisCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("home_open_admin_btn")
                            ) {
                                Text(
                                    text = "Admin Panel",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // 2. Continue Watching (if any)
            if (continueWatchingList.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Continue Watching",
                        subtitle = "Pick up where you left off"
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(continueWatchingList, key = { it.movie.id }) { item ->
                            HangisContinueWatchingCard(
                                item = item,
                                onClick = { onContinueWatchingClick(item) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 3. My Watchlist (if any saved)
            if (watchlistMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "My Watchlist",
                        subtitle = "Saved for later",
                        seeAllText = "View All",
                        onSeeAllClick = { onSeeAllCategory("Watchlist") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(watchlistMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 4. Trending Now
            if (trendingMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Trending Now",
                        subtitle = "What audiences are watching today"
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trendingMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 5. Popular Movies
            item {
                HangisSectionHeader(
                    title = "Popular Movies",
                    subtitle = "Highest community ratings"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularMovies, key = { it.id }) { movie ->
                        HangisMovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 6. Latest Releases
            item {
                HangisSectionHeader(
                    title = "Latest Movies",
                    subtitle = "Freshly premiered releases"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(latestMovies, key = { it.id }) { movie ->
                        HangisMovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 7. TV Shows
            if (tvShows.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "TV Shows & Series",
                        subtitle = "Binge-worthy multi-season sagas",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("TV Shows") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tvShows, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 8. Action Section
            if (actionMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Action & Adventure",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("Action") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(actionMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 9. Sci-Fi Section
            if (sciFiMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Sci-Fi & Cyberpunk",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("Sci-Fi") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sciFiMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 10. Comedy Section
            if (comedyMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Comedy",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("Comedy") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(comedyMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 11. Animation Section
            if (animationMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Animation",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("Animation") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(animationMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 12. Drama Section
            if (dramaMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Drama",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("Drama") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dramaMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 13. Romance Section
            if (romanceMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Romance",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("Romance") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(romanceMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 14. Horror Section
            if (horrorMovies.isNotEmpty()) {
                item {
                    HangisSectionHeader(
                        title = "Horror & Suspense",
                        seeAllText = "See All",
                        onSeeAllClick = { onSeeAllCategory("Horror") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(horrorMovies, key = { it.id }) { movie ->
                            HangisMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }
                }
            }
        }
    }
}

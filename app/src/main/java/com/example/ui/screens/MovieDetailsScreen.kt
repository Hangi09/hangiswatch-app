package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Movie
import com.example.data.model.TvEpisode
import com.example.data.repository.AuthRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.TvShowRepository
import com.example.data.repository.WatchHistoryRepository
import com.example.data.repository.WatchlistRepository
import com.example.ui.components.HangisLoadingIndicator
import com.example.ui.components.HangisMovieCard
import com.example.ui.components.HangisSectionHeader
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisGold
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisSurfaceVariant
import com.example.ui.theme.HangisTextPrimary
import com.example.ui.theme.HangisTextSecondary
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(
    movieId: String,
    movieRepository: MovieRepository,
    watchlistRepository: WatchlistRepository,
    watchHistoryRepository: WatchHistoryRepository,
    tvShowRepository: TvShowRepository,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onWatchClick: (Movie, TvEpisode?) -> Unit,
    onTrailerClick: (Movie) -> Unit,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authRepository.currentUser.collectAsState()

    var movie by remember { mutableStateOf<Movie?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(movieId) {
        isLoading = true
        movie = movieRepository.getMovieById(movieId)
        if (movie != null) {
            movieRepository.incrementViewCount(movieId)
        }
        isLoading = false
    }

    val isInWatchlist by (
        currentUser?.let { user ->
            movie?.let { m -> watchlistRepository.isInWatchlist(user.id, m.id) }
        } ?: kotlinx.coroutines.flow.flowOf(false)
    ).collectAsState(initial = false)

    var watchHistoryItem by remember { mutableStateOf<com.example.data.model.WatchHistoryItem?>(null) }
    LaunchedEffect(currentUser, movie) {
        if (currentUser != null && movie != null) {
            watchHistoryItem = watchHistoryRepository.getHistoryItem(currentUser!!.id, movie!!.id)
        }
    }

    val allEpisodes by (
        movie?.takeIf { it.isTvShow }?.let {
            tvShowRepository.getAllEpisodesForShow(it.id)
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    val allMovies by movieRepository.allPublishedMovies.collectAsState(initial = emptyList())

    if (isLoading || movie == null) {
        HangisLoadingIndicator(message = "Loading movie details...")
        return
    }

    val currentMovie = movie!!
    val similarMovies = allMovies
        .filter { it.id != currentMovie.id && it.genre.equals(currentMovie.genre, ignoreCase = true) }
        .take(6)

    val seasons = allEpisodes.map { it.seasonNumber }.distinct().sorted()
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }
    val currentSeasonNumber = if (seasons.isNotEmpty()) seasons.getOrElse(selectedSeasonIndex) { 1 } else 1
    val episodesForSeason = allEpisodes.filter { it.seasonNumber == currentSeasonNumber }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Backdrop Banner with Header Controls
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentMovie.backdropUrl.ifEmpty { currentMovie.posterUrl })
                            .crossfade(true)
                            .build(),
                        contentDescription = currentMovie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Cinematic Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        HangisBackground.copy(alpha = 0.9f),
                                        HangisBackground
                                    )
                                )
                            )
                    )

                    // Back & Share buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("details_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Watch \"${currentMovie.title}\" on HangisWatch! Highly recommended."
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Movie")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("details_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // 2. Main Title, Meta Badges, and Primary Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = currentMovie.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 26.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Meta Row: Rating, Year, Duration, Genre, 4K Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF261D12))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = HangisGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentMovie.rating}",
                                color = HangisGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${currentMovie.releaseYear}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (currentMovie.isTvShow) "TV Series" else "${currentMovie.durationMinutes} min",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HangisPurple.copy(alpha = 0.3f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = currentMovie.genre,
                                color = HangisPurple,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HangisCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "4K HDR",
                                color = HangisCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons Row: Watch Now / Resume, Add to Watchlist, Trailer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val firstEpisode = if (currentMovie.isTvShow) episodesForSeason.firstOrNull() else null
                                onWatchClick(currentMovie, firstEpisode)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HangisCyan,
                                contentColor = Color(0xFF00222A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("details_watch_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (watchHistoryItem != null && watchHistoryItem!!.completionPercent > 0) "Resume" else "Watch Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                currentUser?.let { user ->
                                    coroutineScope.launch {
                                        watchlistRepository.toggleWatchlist(user.id, currentMovie.id, isInWatchlist)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isInWatchlist) HangisCyan else Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("details_watchlist_button")
                        ) {
                            Icon(
                                imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (isInWatchlist) HangisCyan else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isInWatchlist) "Saved" else "Watchlist",
                                fontSize = 12.sp
                            )
                        }

                        if (currentMovie.trailerUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onTrailerClick(currentMovie) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(48.dp)
                                    .testTag("details_trailer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trailer", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Synopsis Card
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentMovie.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HangisTextSecondary,
                        lineHeight = 22.sp,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Production Details (Director, Cast, Subtitles)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row {
                                Text(
                                    text = "Director: ",
                                    color = HangisTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentMovie.director,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            Row {
                                Text(
                                    text = "Starring: ",
                                    color = HangisTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentMovie.cast,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            Row {
                                Text(
                                    text = "Language: ",
                                    color = HangisTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentMovie.language,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            Row {
                                Text(
                                    text = "Subtitles: ",
                                    color = HangisTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentMovie.subtitles,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. TV Show: Seasons & Episode Selector (if TV Show)
            if (currentMovie.isTvShow && seasons.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Episodes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Season Tabs
                    TabRow(
                        selectedTabIndex = selectedSeasonIndex,
                        containerColor = Color.Transparent,
                        contentColor = HangisCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedSeasonIndex]),
                                color = HangisCyan
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        seasons.forEachIndexed { index, seasonNum ->
                            Tab(
                                selected = selectedSeasonIndex == index,
                                onClick = { selectedSeasonIndex = index },
                                text = {
                                    Text(
                                        text = "Season $seasonNum",
                                        fontWeight = if (selectedSeasonIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSeasonIndex == index) HangisCyan else HangisTextSecondary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(episodesForSeason, key = { it.id }) { episode ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clickable { onWatchClick(currentMovie, episode) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(HangisCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Episode",
                                    tint = HangisCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "E${episode.episodeNumber}: ${episode.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${episode.durationMinutes} min • ${episode.description}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HangisTextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Similar Movies Row
            if (similarMovies.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HangisSectionHeader(
                        title = "More Like This",
                        subtitle = "Recommended for fans of ${currentMovie.genre}"
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(similarMovies, key = { it.id }) { similar ->
                            HangisMovieCard(
                                movie = similar,
                                onClick = { onMovieClick(similar) }
                            )
                        }
                    }
                }
            }
        }
    }
}

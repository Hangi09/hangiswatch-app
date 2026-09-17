package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.HangisDatabase
import com.example.data.local.UserEntity
import com.example.data.model.Movie
import com.example.data.model.TvEpisode
import com.example.data.repository.MovieRepository
import com.example.data.repository.SubscriptionRepository
import com.example.data.repository.TvShowRepository
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisRed
import com.example.ui.theme.HangisSurfaceVariant
import com.example.ui.theme.HangisTextSecondary
import com.example.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    movieRepository: MovieRepository,
    tvShowRepository: TvShowRepository,
    subscriptionRepository: SubscriptionRepository,
    database: HangisDatabase,
    onBackClick: () -> Unit,
    onLogoutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sitePrefs = remember { context.getSharedPreferences("hangis_site_settings", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        "Overview",
        "Movies",
        "TV Shows",
        "Categories",
        "Users & Subs",
        "Analytics & Payments",
        "Site Settings"
    )

    val movies by movieRepository.allMoviesForAdmin.collectAsState(initial = emptyList())
    val movieCount by movieRepository.getMovieCount().collectAsState(initial = 0)
    val tvShowCount by movieRepository.getTvShowCount().collectAsState(initial = 0)
    val totalRevenue by subscriptionRepository.getTotalRevenue().collectAsState(initial = 0.0)
    val users by database.userDao().getAllUsers().collectAsState(initial = emptyList())
    val payments by database.paymentDao().getAllPayments().collectAsState(initial = emptyList())

    val totalViews = remember(movies) { movies.sumOf { it.viewCount } }

    // Search filters
    var movieSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var userSearchQuery by remember { mutableStateOf("") }

    // Movie Dialog State (Add & Edit)
    var showMovieDialog by remember { mutableStateOf(false) }
    var movieToEdit by remember { mutableStateOf<Movie?>(null) }
    var movieTitle by remember { mutableStateOf("") }
    var movieDesc by remember { mutableStateOf("") }
    var movieGenre by remember { mutableStateOf("") }
    var movieYear by remember { mutableStateOf("2025") }
    var movieDuration by remember { mutableStateOf("") }
    var movieRating by remember { mutableStateOf("") }
    var movieCast by remember { mutableStateOf("") }
    var movieDirector by remember { mutableStateOf("") }
    var moviePosterUrl by remember { mutableStateOf("") }
    var movieBackdropUrl by remember { mutableStateOf("") }
    var movieVideoUrl by remember { mutableStateOf("") }
    var movieTrailerUrl by remember { mutableStateOf("") }
    var movieSubtitles by remember { mutableStateOf("") }
    var movieLanguage by remember { mutableStateOf("English") }
    var movieIsTvShow by remember { mutableStateOf(false) }
    var movieIsFeatured by remember { mutableStateOf(false) }
    var movieIsTrending by remember { mutableStateOf(false) }
    var movieIsPublished by remember { mutableStateOf(true) }

    // Delete Confirmation Dialog
    var movieToDelete by remember { mutableStateOf<Movie?>(null) }

    // Episode Dialog State
    var showEpisodeDialog by remember { mutableStateOf(false) }
    var episodeTvShowId by remember { mutableStateOf("") }
    var episodeSeason by remember { mutableStateOf("1") }
    var episodeNumber by remember { mutableStateOf("1") }
    var episodeTitle by remember { mutableStateOf("") }
    var episodeDesc by remember { mutableStateOf("") }
    var episodeDuration by remember { mutableStateOf("45") }
    var episodeVideoUrl by remember { mutableStateOf("") }

    val tvShowsList = movies.filter { it.isTvShow }
    val selectedTvShowForEpisodes = tvShowsList.firstOrNull { it.id == episodeTvShowId } ?: tvShowsList.firstOrNull()

    val episodes by (
        selectedTvShowForEpisodes?.let { tvShowRepository.getAllEpisodesForShow(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    // User Reset Password Dialog
    var userToResetPassword by remember { mutableStateOf<UserEntity?>(null) }
    var newPasswordForUser by remember { mutableStateOf("") }

    // Site Settings State
    var allowRegistrations by remember {
        mutableStateOf(sitePrefs.getBoolean("allow_registrations", true))
    }
    var maintenanceMode by remember {
        mutableStateOf(sitePrefs.getBoolean("maintenance_mode", false))
    }
    var defaultQuality by remember {
        mutableStateOf(sitePrefs.getString("default_quality", "1080p") ?: "1080p")
    }
    var announcementBanner by remember {
        mutableStateOf(sitePrefs.getString("announcement_banner", "Welcome to HangisWatch Premium Streaming") ?: "")
    }
    var showSettingsSavedMsg by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
            .testTag("admin_dashboard_root")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Admin Control Panel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HangisPurple)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ADMIN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Connected to Room Database • admin@hangiswatch.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HangisCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                if (onLogoutClick != null) {
                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout Admin",
                            tint = HangisRed
                        )
                    }
                }
            }

            // Navigation Tabs (Scrollable)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = HangisBackground,
                contentColor = HangisCyan,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = HangisCyan
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) HangisCyan else HangisTextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            when (selectedTab) {
                // -------------------------------------------------------------
                // 0: Overview (Dashboard Overview & Shortcuts)
                // -------------------------------------------------------------
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        // System Status Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = HangisCyan, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("HangisWatch Core Status: Online", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Room Database connected • SHA-256 Auth Active • ExoPlayer Engine Ready", color = HangisTextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatCard(title = "Movies", value = "$movieCount", icon = Icons.Default.Movie, color = HangisCyan, modifier = Modifier.weight(1f))
                            AdminStatCard(title = "TV Series", value = "$tvShowCount", icon = Icons.Default.Tv, color = HangisPurple, modifier = Modifier.weight(1f))
                            AdminStatCard(title = "Users", value = "${users.size}", icon = Icons.Default.Group, color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                            AdminStatCard(title = "Revenue", value = "$${String.format("%.0f", totalRevenue)}", icon = Icons.Default.AttachMoney, color = Color(0xFF34D399), modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatCard(title = "Total Views", value = "$totalViews", icon = Icons.Default.Visibility, color = Color(0xFFFBBF24), modifier = Modifier.weight(1f))
                            AdminStatCard(title = "Orders", value = "${payments.size}", icon = Icons.Default.Payment, color = Color(0xFFA78BFA), modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Actions
                        Text("Quick Management Actions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    movieToEdit = null
                                    resetMovieFormState { title, desc, genre, year, dur, rating, cast, director, poster, backdrop, video, trailer, subtitles, lang, isTv, isFeat, isTrend, isPub ->
                                        movieTitle = title; movieDesc = desc; movieGenre = genre; movieYear = year
                                        movieDuration = dur; movieRating = rating; movieCast = cast; movieDirector = director
                                        moviePosterUrl = poster; movieBackdropUrl = backdrop; movieVideoUrl = video
                                        movieTrailerUrl = trailer; movieSubtitles = subtitles; movieLanguage = lang
                                        movieIsTvShow = isTv; movieIsFeatured = isFeat; movieIsTrending = isTrend; movieIsPublished = isPub
                                    }
                                    showMovieDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HangisCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("overview_add_movie_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Title", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { selectedTab = 4 },
                                colors = ButtonDefaults.buttonColors(containerColor = HangisPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Users", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { selectedTab = 5 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Payments", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Recent Users
                        Text("Recent Registered Users", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        users.take(3).forEach { u ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(HangisPurple.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(u.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(u.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(u.email, color = HangisTextSecondary, fontSize = 11.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (u.role == "ADMIN") HangisPurple else HangisSurfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(u.role, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                // -------------------------------------------------------------
                // 1: Movies (Add, Edit, Delete, Featured/Trending Toggles)
                // -------------------------------------------------------------
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Header with Add Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Movies & Titles (${movies.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    movieToEdit = null
                                    resetMovieFormState { title, desc, genre, year, dur, rating, cast, director, poster, backdrop, video, trailer, subtitles, lang, isTv, isFeat, isTrend, isPub ->
                                        movieTitle = title; movieDesc = desc; movieGenre = genre; movieYear = year
                                        movieDuration = dur; movieRating = rating; movieCast = cast; movieDirector = director
                                        moviePosterUrl = poster; movieBackdropUrl = backdrop; movieVideoUrl = video
                                        movieTrailerUrl = trailer; movieSubtitles = subtitles; movieLanguage = lang
                                        movieIsTvShow = isTv; movieIsFeatured = isFeat; movieIsTrending = isTrend; movieIsPublished = isPub
                                    }
                                    showMovieDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HangisCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("admin_add_movie_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Title", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Input
                        OutlinedTextField(
                            value = movieSearchQuery,
                            onValueChange = { movieSearchQuery = it },
                            placeholder = { Text("Search titles or genres...", color = HangisTextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HangisCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HangisCyan,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_movie_search_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredMovies = movies.filter {
                            (movieSearchQuery.isEmpty() || it.title.contains(movieSearchQuery, ignoreCase = true) || it.genre.contains(movieSearchQuery, ignoreCase = true)) &&
                            (selectedCategoryFilter == "All" || it.genre.contains(selectedCategoryFilter, ignoreCase = true))
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredMovies, key = { it.id }) { movie ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_movie_item_${movie.id}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Thumbnail
                                            AsyncImage(
                                                model = movie.posterUrl,
                                                contentDescription = movie.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(width = 50.dp, height = 75.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.DarkGray)
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = movie.title,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (movie.isTvShow) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(HangisPurple)
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("TV", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = "${movie.releaseYear} • ${movie.genre} • ★ ${movie.rating}",
                                                    color = HangisTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "${movie.viewCount} views • ${movie.language}",
                                                    color = HangisCyan,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            // Action Buttons: Edit & Delete
                                            IconButton(
                                                onClick = {
                                                    movieToEdit = movie
                                                    movieTitle = movie.title
                                                    movieDesc = movie.description
                                                    movieGenre = movie.genre
                                                    movieYear = movie.releaseYear.toString()
                                                    movieDuration = movie.durationMinutes.toString()
                                                    movieRating = movie.rating.toString()
                                                    movieCast = movie.cast
                                                    movieDirector = movie.director
                                                    moviePosterUrl = movie.posterUrl
                                                    movieBackdropUrl = movie.backdropUrl
                                                    movieVideoUrl = movie.videoUrl
                                                    movieTrailerUrl = movie.trailerUrl
                                                    movieSubtitles = movie.subtitles
                                                    movieLanguage = movie.language
                                                    movieIsTvShow = movie.isTvShow
                                                    movieIsFeatured = movie.isFeatured
                                                    movieIsTrending = movie.isTrending
                                                    movieIsPublished = movie.isPublished
                                                    showMovieDialog = true
                                                },
                                                modifier = Modifier.size(36.dp).testTag("edit_movie_${movie.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit movie", tint = HangisCyan, modifier = Modifier.size(20.dp))
                                            }

                                            IconButton(
                                                onClick = { movieToDelete = movie },
                                                modifier = Modifier.size(36.dp).testTag("delete_movie_${movie.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete movie", tint = HangisRed, modifier = Modifier.size(20.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Featured / Trending / Published Quick Controls
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Switch(
                                                    checked = movie.isFeatured,
                                                    onCheckedChange = { checked ->
                                                        coroutineScope.launch {
                                                            movieRepository.updateMovie(movie.copy(isFeatured = checked))
                                                        }
                                                    },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = HangisCyan, checkedTrackColor = HangisCyan.copy(alpha = 0.4f)),
                                                    modifier = Modifier.size(32.dp).testTag("toggle_featured_${movie.id}")
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Featured", fontSize = 11.sp, color = if (movie.isFeatured) HangisCyan else HangisTextSecondary)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Switch(
                                                    checked = movie.isTrending,
                                                    onCheckedChange = { checked ->
                                                        coroutineScope.launch {
                                                            movieRepository.updateMovie(movie.copy(isTrending = checked))
                                                        }
                                                    },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = HangisPurple, checkedTrackColor = HangisPurple.copy(alpha = 0.4f)),
                                                    modifier = Modifier.size(32.dp).testTag("toggle_trending_${movie.id}")
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Trending", fontSize = 11.sp, color = if (movie.isTrending) HangisPurple else HangisTextSecondary)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Switch(
                                                    checked = movie.isPublished,
                                                    onCheckedChange = { checked ->
                                                        coroutineScope.launch {
                                                            movieRepository.updateMovie(movie.copy(isPublished = checked))
                                                        }
                                                    },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF34D399), checkedTrackColor = Color(0xFF34D399).copy(alpha = 0.4f)),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Live", fontSize = 11.sp, color = if (movie.isPublished) Color(0xFF34D399) else HangisTextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 2: TV Shows & Episodes Management
                // -------------------------------------------------------------
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TV Shows & Episodes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (selectedTvShowForEpisodes != null) {
                                Button(
                                    onClick = {
                                        episodeTvShowId = selectedTvShowForEpisodes.id
                                        episodeTitle = ""
                                        episodeDesc = ""
                                        episodeSeason = "1"
                                        episodeNumber = "${episodes.size + 1}"
                                        episodeDuration = "45"
                                        episodeVideoUrl = "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4"
                                        showEpisodeDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HangisPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("admin_add_episode_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Episode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Series Selector Pills
                        Text("Select Series:", color = HangisTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(tvShowsList) { show ->
                                val isSelected = show.id == selectedTvShowForEpisodes?.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) HangisPurple else HangisCardBg)
                                        .clickable { episodeTvShowId = show.id }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = show.title,
                                        color = if (isSelected) Color.White else HangisTextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedTvShowForEpisodes == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No TV Shows available. Add one in the Movies tab by toggling 'TV Show'.", color = HangisTextSecondary, fontSize = 13.sp)
                            }
                        } else {
                            Text(
                                text = "Episodes for ${selectedTvShowForEpisodes.title} (${episodes.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 90.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(episodes, key = { it.id }) { ep ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "S${ep.seasonNumber}:E${ep.episodeNumber} - ${ep.title}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "${ep.durationMinutes} min • ${ep.description}",
                                                    color = HangisTextSecondary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        tvShowRepository.deleteEpisode(ep.id)
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete episode", tint = HangisRed, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 3: Categories Management
                // -------------------------------------------------------------
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Categories & Genres Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Manage and view distribution across streaming genres",
                            color = HangisTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val primaryCategories = listOf(
                            "Action", "Sci-Fi", "Horror", "Animation", "Drama",
                            "Comedy", "Thriller", "Adventure", "Romance", "Fantasy", "Documentary"
                        )

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(primaryCategories) { cat ->
                                val count = movies.count { it.genre.contains(cat, ignoreCase = true) }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(HangisCyan.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Category, contentDescription = null, tint = HangisCyan, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(cat, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("$count titles assigned", color = HangisTextSecondary, fontSize = 11.sp)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                selectedCategoryFilter = cat
                                                selectedTab = 1
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HangisCyan),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Filter Titles", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 4: Users Management & Subscriptions
                // -------------------------------------------------------------
                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Users & Subscriptions (${users.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search users by email or name...", color = HangisTextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HangisCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HangisCyan,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_user_search_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredUsers = users.filter {
                            userSearchQuery.isEmpty() ||
                            it.email.contains(userSearchQuery, ignoreCase = true) ||
                            it.name.contains(userSearchQuery, ignoreCase = true)
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredUsers, key = { it.id }) { u ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_user_item_${u.id}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(if (u.role == "ADMIN") HangisPurple.copy(alpha = 0.3f) else HangisCyan.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(u.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = u.name,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 14.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (u.role == "ADMIN") HangisPurple else Color(0xFF334155))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(u.role, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Text(u.email, color = HangisTextSecondary, fontSize = 12.sp)
                                                Text("Tier: ${u.subscriptionTier} • Status: ${if (u.isActive) "Active" else "Suspended"}", color = if (u.subscriptionTier == "PREMIUM") HangisCyan else HangisTextSecondary, fontSize = 11.sp)
                                            }

                                            // Password reset key icon
                                            IconButton(
                                                onClick = {
                                                    userToResetPassword = u
                                                    newPasswordForUser = ""
                                                },
                                                modifier = Modifier.size(32.dp).testTag("reset_pwd_${u.id}")
                                            ) {
                                                Icon(Icons.Default.Key, contentDescription = "Reset Password", tint = HangisCyan, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Role, Tier, and Ban Action Controls
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Toggle Role (USER <-> ADMIN)
                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        val newRole = if (u.role == "ADMIN") "USER" else "ADMIN"
                                                        database.userDao().updateRole(u.id, newRole)
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("role_btn_${u.id}")
                                            ) {
                                                Text(if (u.role == "ADMIN") "Make User" else "Make Admin", fontSize = 10.sp)
                                            }

                                            // Toggle Subscription (FREE <-> PREMIUM)
                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        val newTier = if (u.subscriptionTier == "PREMIUM") "FREE" else "PREMIUM"
                                                        val expires = if (newTier == "PREMIUM") System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000 else null
                                                        database.userDao().updateSubscription(u.id, newTier, expires)
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("tier_btn_${u.id}")
                                            ) {
                                                Text(if (u.subscriptionTier == "PREMIUM") "Downgrade" else "Upgrade Prem", fontSize = 10.sp)
                                            }

                                            // Toggle Active / Suspended
                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        database.userDao().updateActiveStatus(u.id, !u.isActive)
                                                    }
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (u.isActive) HangisRed else Color(0xFF34D399)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).testTag("status_btn_${u.id}")
                                            ) {
                                                Text(if (u.isActive) "Suspend" else "Activate", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 5: Analytics & Payments
                // -------------------------------------------------------------
                5 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Watch Statistics & Payment Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Top Watched Titles
                        Text("Top Watched Titles", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val topWatched = movies.sortedByDescending { it.viewCount }.take(5)
                        val maxViews = (topWatched.maxOfOrNull { it.viewCount } ?: 1).coerceAtLeast(1)

                        topWatched.forEach { movie ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(movie.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${movie.viewCount} views", color = HangisCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { movie.viewCount.toFloat() / maxViews },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = HangisCyan,
                                    trackColor = Color(0xFF1E293B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Payment & Subscription Transactions
                        Text("Subscription & Payment Transactions (${payments.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (payments.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No recorded payment transactions yet. When users subscribe in the Subscription screen, transactions appear here in real-time.",
                                    color = HangisTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            payments.forEach { pay ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(pay.transactionRef, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("User: ${pay.userId} • ${pay.planTier}", color = HangisTextSecondary, fontSize = 11.sp)
                                            Text(dateFormat.format(Date(pay.date)), color = Color(0xFF64748B), fontSize = 10.sp)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("$${String.format("%.2f", pay.amount)}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF065F46))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(pay.status, color = Color(0xFF6EE7B7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }

                // -------------------------------------------------------------
                // 6: Site Settings
                // -------------------------------------------------------------
                6 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Site & Platform Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Configure platform behaviors and streaming defaults",
                            color = HangisTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Allow Registrations
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Allow User Signups", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Enable new users to register via the auth screen", color = HangisTextSecondary, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = allowRegistrations,
                                        onCheckedChange = {
                                            allowRegistrations = it
                                            sitePrefs.edit().putBoolean("allow_registrations", it).apply()
                                            showSettingsSavedMsg = true
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = HangisCyan, checkedTrackColor = HangisCyan.copy(alpha = 0.4f))
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Maintenance Mode
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Maintenance Mode", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Show maintenance notice to non-admin users", color = HangisTextSecondary, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = maintenanceMode,
                                        onCheckedChange = {
                                            maintenanceMode = it
                                            sitePrefs.edit().putBoolean("maintenance_mode", it).apply()
                                            showSettingsSavedMsg = true
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = HangisRed, checkedTrackColor = HangisRed.copy(alpha = 0.4f))
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Announcement Banner
                                Text("Global Announcement Banner", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = announcementBanner,
                                    onValueChange = {
                                        announcementBanner = it
                                        sitePrefs.edit().putString("announcement_banner", it).apply()
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HangisCyan,
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Streaming Quality Default
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Default Streaming Quality", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Auto", "720p", "1080p", "4K Ultra").forEach { q ->
                                        val isSel = defaultQuality == q
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) HangisCyan else HangisSurfaceVariant)
                                                .clickable {
                                                    defaultQuality = q
                                                    sitePrefs.edit().putString("default_quality", q).apply()
                                                    showSettingsSavedMsg = true
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(q, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (showSettingsSavedMsg) {
                            Text("✓ Settings updated successfully", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // Dialog: Add / Edit Movie
        // ---------------------------------------------------------------------
        if (showMovieDialog) {
            AlertDialog(
                onDismissRequest = { showMovieDialog = false },
                title = {
                    Text(
                        text = if (movieToEdit != null) "Edit Title" else "Add New Title",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = movieTitle,
                            onValueChange = { movieTitle = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("movie_title_input")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = movieDesc,
                            onValueChange = { movieDesc = it },
                            label = { Text("Description") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("movie_desc_input")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = movieGenre,
                                onValueChange = { movieGenre = it },
                                label = { Text("Genre") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("movie_genre_input")
                            )
                            OutlinedTextField(
                                value = movieYear,
                                onValueChange = { movieYear = it },
                                label = { Text("Year") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("movie_year_input")
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = movieDuration,
                                onValueChange = { movieDuration = it },
                                label = { Text("Duration (m)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = movieRating,
                                onValueChange = { movieRating = it },
                                label = { Text("Rating (0-10)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = moviePosterUrl,
                            onValueChange = { moviePosterUrl = it },
                            label = { Text("Poster URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = movieVideoUrl,
                            onValueChange = { movieVideoUrl = it },
                            label = { Text("Video URL (.mp4 / HLS)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("movie_video_url_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Checkboxes
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = movieIsTvShow,
                                onCheckedChange = { movieIsTvShow = it },
                                colors = CheckboxDefaults.colors(checkedColor = HangisPurple)
                            )
                            Text("Is TV Show", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Checkbox(
                                checked = movieIsFeatured,
                                onCheckedChange = { movieIsFeatured = it },
                                colors = CheckboxDefaults.colors(checkedColor = HangisCyan)
                            )
                            Text("Featured", color = Color.White, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val year = movieYear.toIntOrNull() ?: 2025
                            val dur = movieDuration.toIntOrNull() ?: 110
                            val rat = movieRating.toDoubleOrNull() ?: 8.0
                            val fallbackVideo = if (movieVideoUrl.isBlank()) "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4" else movieVideoUrl
                            val fallbackPoster = if (moviePosterUrl.isBlank()) "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600" else moviePosterUrl

                            if (movieToEdit != null) {
                                val updated = movieToEdit!!.copy(
                                    title = movieTitle.ifBlank { "Untitled" },
                                    description = movieDesc.ifBlank { "No description" },
                                    genre = movieGenre.ifBlank { "Action" },
                                    releaseYear = year,
                                    durationMinutes = dur,
                                    rating = rat,
                                    posterUrl = fallbackPoster,
                                    videoUrl = fallbackVideo,
                                    isTvShow = movieIsTvShow,
                                    isFeatured = movieIsFeatured,
                                    isTrending = movieIsTrending,
                                    isPublished = movieIsPublished
                                )
                                coroutineScope.launch {
                                    movieRepository.updateMovie(updated)
                                }
                            } else {
                                val newId = "custom_${System.currentTimeMillis()}"
                                val newMovie = Movie(
                                    id = newId,
                                    title = movieTitle.ifBlank { "New Movie" },
                                    description = movieDesc.ifBlank { "Added via Admin Panel" },
                                    genre = movieGenre.ifBlank { "Action" },
                                    releaseYear = year,
                                    durationMinutes = dur,
                                    rating = rat,
                                    cast = movieCast.ifBlank { "Cast" },
                                    director = movieDirector.ifBlank { "Director" },
                                    posterUrl = fallbackPoster,
                                    backdropUrl = movieBackdropUrl.ifBlank { fallbackPoster },
                                    videoUrl = fallbackVideo,
                                    trailerUrl = movieTrailerUrl,
                                    subtitles = movieSubtitles,
                                    language = movieLanguage.ifBlank { "English" },
                                    isTvShow = movieIsTvShow,
                                    isFeatured = movieIsFeatured,
                                    isTrending = movieIsTrending,
                                    isPublished = movieIsPublished
                                )
                                coroutineScope.launch {
                                    movieRepository.insertMovie(newMovie)
                                }
                            }
                            showMovieDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisCyan),
                        modifier = Modifier.testTag("admin_save_movie_btn")
                    ) {
                        Text("Save Title", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMovieDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }

        // ---------------------------------------------------------------------
        // Dialog: Delete Movie Confirmation
        // ---------------------------------------------------------------------
        if (movieToDelete != null) {
            AlertDialog(
                onDismissRequest = { movieToDelete = null },
                title = { Text("Delete Title", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to permanently delete \"${movieToDelete!!.title}\"? This action will remove it from the catalog, watchlists, and watch history.",
                        color = HangisTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = movieToDelete!!.id
                            coroutineScope.launch {
                                movieRepository.deleteMovie(id)
                            }
                            movieToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisRed),
                        modifier = Modifier.testTag("confirm_delete_movie_btn")
                    ) {
                        Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { movieToDelete = null }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }

        // ---------------------------------------------------------------------
        // Dialog: Add TV Episode
        // ---------------------------------------------------------------------
        if (showEpisodeDialog) {
            AlertDialog(
                onDismissRequest = { showEpisodeDialog = false },
                title = { Text("Add TV Episode", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = episodeTitle,
                            onValueChange = { episodeTitle = it },
                            label = { Text("Episode Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = episodeSeason,
                                onValueChange = { episodeSeason = it },
                                label = { Text("Season") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = episodeNumber,
                                onValueChange = { episodeNumber = it },
                                label = { Text("Episode #") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = episodeVideoUrl,
                            onValueChange = { episodeVideoUrl = it },
                            label = { Text("Video URL (.mp4 / HLS)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (episodeTvShowId.isNotBlank()) {
                                val ep = TvEpisode(
                                    id = "ep_${System.currentTimeMillis()}",
                                    tvShowId = episodeTvShowId,
                                    seasonNumber = episodeSeason.toIntOrNull() ?: 1,
                                    episodeNumber = episodeNumber.toIntOrNull() ?: 1,
                                    title = episodeTitle.ifBlank { "Episode $episodeNumber" },
                                    description = episodeDesc.ifBlank { "Episode description" },
                                    durationMinutes = episodeDuration.toIntOrNull() ?: 45,
                                    videoUrl = episodeVideoUrl.ifBlank { "https://archive.org/download/Tears-of-Steel/tears_of_steel_720p.mp4" }
                                )
                                coroutineScope.launch {
                                    tvShowRepository.insertEpisode(ep)
                                }
                            }
                            showEpisodeDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisPurple)
                    ) {
                        Text("Add Episode", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEpisodeDialog = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }

        // ---------------------------------------------------------------------
        // Dialog: User Reset Password
        // ---------------------------------------------------------------------
        if (userToResetPassword != null) {
            AlertDialog(
                onDismissRequest = { userToResetPassword = null },
                title = { Text("Reset User Password", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Enter a new password for ${userToResetPassword!!.email}:", color = HangisTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPasswordForUser,
                            onValueChange = { newPasswordForUser = it },
                            label = { Text("New Password (min 6 chars)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_new_user_pwd_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPasswordForUser.length >= 6) {
                                val user = userToResetPassword!!
                                coroutineScope.launch(Dispatchers.IO) {
                                    val salt = SecurityUtils.generateSalt()
                                    val hash = SecurityUtils.hashPassword(newPasswordForUser, salt)
                                    database.userDao().updatePassword(user.id, hash, salt)
                                }
                                userToResetPassword = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HangisCyan),
                        modifier = Modifier.testTag("admin_confirm_reset_pwd_btn")
                    ) {
                        Text("Set Password", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToResetPassword = null }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = HangisCardBg
            )
        }
    }
}

private inline fun resetMovieFormState(
    crossinline applyState: (
        title: String, desc: String, genre: String, year: String, dur: String, rating: String,
        cast: String, director: String, poster: String, backdrop: String, video: String, trailer: String,
        subtitles: String, lang: String, isTv: Boolean, isFeat: Boolean, isTrend: Boolean, isPub: Boolean
    ) -> Unit
) {
    applyState(
        "", "", "", "2025", "", "", "", "", "", "", "", "", "", "English", false, false, false, true
    )
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HangisCardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
            Text(text = title, fontSize = 10.sp, color = HangisTextSecondary)
        }
    }
}

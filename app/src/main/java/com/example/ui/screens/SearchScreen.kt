package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.ui.components.EmptyStateType
import com.example.ui.components.HangisEmptyState
import com.example.ui.components.HangisMovieCard
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisSurfaceVariant
import com.example.ui.theme.HangisTextPrimary
import com.example.ui.theme.HangisTextSecondary

@Composable
fun SearchScreen(
    movieRepository: MovieRepository,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }
    var recentSearches by remember { mutableStateOf(listOf("Cyberpunk", "Cosmic", "Dragon", "Comedy")) }

    val allMovies by movieRepository.allPublishedMovies.collectAsState(initial = emptyList())

    val genres = listOf("All", "Sci-Fi", "Action", "Drama", "Animation", "Comedy", "Horror", "Romance", "TV Shows")

    val filteredMovies = allMovies.filter { movie ->
        val matchesQuery = if (searchQuery.isBlank()) {
            true
        } else {
            movie.title.contains(searchQuery, ignoreCase = true) ||
            movie.cast.contains(searchQuery, ignoreCase = true) ||
            movie.director.contains(searchQuery, ignoreCase = true) ||
            movie.genre.contains(searchQuery, ignoreCase = true) ||
            movie.description.contains(searchQuery, ignoreCase = true)
        }

        val matchesGenre = if (selectedGenre == "All") true else {
            movie.genre.contains(selectedGenre, ignoreCase = true)
        }

        matchesQuery && matchesGenre
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            // Search Input Field
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search titles, actors, directors, genres...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = HangisCyan
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.LightGray
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HangisCyan,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = HangisSurfaceVariant,
                        unfocusedContainerColor = HangisSurfaceVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Genre filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    val isSelected = selectedGenre == genre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) HangisCyan else HangisSurfaceVariant
                            )
                            .clickable { selectedGenre = genre }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = genre,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF00222A) else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Popular / Recent searches suggestions when search is empty
            if (searchQuery.isEmpty() && selectedGenre == "All") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = HangisPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trending Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Tears of Steel", "Odyssey", "Bunny", "Aethelgard", "Action 2025")) { trend ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable { searchQuery = trend }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = trend, color = HangisCyan, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Results Header
            Text(
                text = if (searchQuery.isBlank() && selectedGenre == "All") "Browse Catalog" else "Results (${filteredMovies.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HangisTextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Grid Results or Empty State
            if (filteredMovies.isEmpty()) {
                HangisEmptyState(
                    type = EmptyStateType.SEARCH,
                    customMessage = "No movies or shows found for \"$searchQuery\". Try another keyword or genre.",
                    actionLabel = "Clear Filter",
                    onActionClick = {
                        searchQuery = ""
                        selectedGenre = "All"
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMovies, key = { it.id }) { movie ->
                        HangisMovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            width = 110.dp,
                            height = 165.dp
                        )
                    }
                }
            }
        }
    }
}

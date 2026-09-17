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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.HangisTextPrimary
import com.example.ui.theme.HangisTextSecondary

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

@Composable
fun CategoriesScreen(
    movieRepository: MovieRepository,
    onMovieClick: (Movie) -> Unit,
    initialCategory: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val allMovies by movieRepository.allPublishedMovies.collectAsState(initial = emptyList())

    val categories = listOf(
        CategoryItem("Action", Icons.Default.Bolt, listOf(Color(0xFFE11D48), Color(0xFFF97316))),
        CategoryItem("Adventure", Icons.Default.Explore, listOf(Color(0xFFD97706), Color(0xFFEAB308))),
        CategoryItem("Comedy", Icons.Default.SentimentVerySatisfied, listOf(Color(0xFF059669), Color(0xFF10B981))),
        CategoryItem("Drama", Icons.Default.MenuBook, listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6))),
        CategoryItem("Horror", Icons.Default.NightlightRound, listOf(Color(0xFF1E293B), Color(0xFF475569))),
        CategoryItem("Romance", Icons.Default.Favorite, listOf(Color(0xFFDB2777), Color(0xFFEC4899))),
        CategoryItem("Sci-Fi", Icons.Default.RocketLaunch, listOf(Color(0xFF0284C7), Color(0xFF06B6D4))),
        CategoryItem("Thriller", Icons.Default.Psychology, listOf(Color(0xFF9333EA), Color(0xFFC084FC))),
        CategoryItem("Animation", Icons.Default.Animation, listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF))),
        CategoryItem("Documentary", Icons.Default.LocalMovies, listOf(Color(0xFF4B5563), Color(0xFF6B7280))),
        CategoryItem("Family", Icons.Default.ChildCare, listOf(Color(0xFF2563EB), Color(0xFF60A5FA))),
        CategoryItem("Crime", Icons.Default.FlashOn, listOf(Color(0xFF991B1B), Color(0xFFEF4444))),
        CategoryItem("TV Shows", Icons.Default.Tv, listOf(Color(0xFF6D28D9), Color(0xFFA855F7)))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        if (selectedCategory != null) {
            // Viewing movies within selected category
            val categoryMovies = allMovies.filter {
                if (selectedCategory == "TV Shows") {
                    it.isTvShow || it.genre.contains("TV", ignoreCase = true)
                } else {
                    it.genre.contains(selectedCategory!!, ignoreCase = true)
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedCategory = null },
                        modifier = Modifier.testTag("category_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedCategory!!,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${categoryMovies.size} titles)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HangisTextSecondary
                    )
                }

                if (categoryMovies.isEmpty()) {
                    HangisEmptyState(
                        type = EmptyStateType.CATEGORY,
                        customMessage = "No titles are currently published in $selectedCategory. Check back soon!"
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(110.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categoryMovies, key = { it.id }) { movie ->
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
        } else {
            // Main Category Grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Explore Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Browse by cinematic genre and format",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HangisTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { cat ->
                        val count = allMovies.count {
                            if (cat.name == "TV Shows") it.isTvShow || it.genre.contains("TV", ignoreCase = true)
                            else it.genre.contains(cat.name, ignoreCase = true)
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F2E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clickable { selectedCategory = cat.name }
                                .testTag("category_tile_${cat.name}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(cat.gradient)
                                    )
                                    .padding(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                                    Text(
                                        text = cat.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "$count titles",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

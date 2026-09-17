package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.repository.AuthRepository
import com.example.data.repository.ContinueWatchingItem
import com.example.data.repository.WatchHistoryRepository
import com.example.ui.components.EmptyStateType
import com.example.ui.components.HangisEmptyState
import com.example.ui.theme.HangisBackground
import com.example.ui.theme.HangisCardBg
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisRed
import com.example.ui.theme.HangisTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WatchHistoryScreen(
    watchHistoryRepository: WatchHistoryRepository,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onResumeClick: (ContinueWatchingItem) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authRepository.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val user = currentUser

    val historyItems by (
        user?.let { watchHistoryRepository.getContinueWatching(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HangisBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Watch History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${historyItems.size} titles in progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HangisTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (historyItems.isEmpty()) {
                HangisEmptyState(
                    type = EmptyStateType.HISTORY,
                    actionLabel = "Start Streaming",
                    onActionClick = onExploreClick
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyItems, key = { it.movie.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = HangisCardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_item_${item.movie.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(100.dp, 64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.movie.backdropUrl.ifEmpty { item.movie.posterUrl })
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = item.movie.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.movie.title,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { item.history.completionPercent / 100f },
                                        color = HangisCyan,
                                        trackColor = Color(0xFF334155),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.history.lastWatchedAt))
                                    Text(
                                        text = "${item.history.completionPercent}% completed • $dateStr",
                                        color = HangisTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Resume button
                                IconButton(
                                    onClick = { onResumeClick(item) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HangisCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = Color(0xFF00222A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Delete history button
                                IconButton(
                                    onClick = {
                                        if (user != null) {
                                            coroutineScope.launch {
                                                watchHistoryRepository.clearHistory(user.id, item.movie.id)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove history",
                                        tint = HangisTextSecondary,
                                        modifier = Modifier.size(18.dp)
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

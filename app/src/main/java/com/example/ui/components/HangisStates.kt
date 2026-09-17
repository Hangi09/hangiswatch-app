package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisPurple
import com.example.ui.theme.HangisSurfaceVariant
import com.example.ui.theme.HangisTextPrimary
import com.example.ui.theme.HangisTextSecondary

@Composable
fun HangisLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = HangisCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = HangisTextSecondary
            )
        }
    }
}

@Composable
fun HangisErrorState(
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    message: String = "Unable to load content right now. Please check your connection and try again.",
    icon: ImageVector = Icons.Default.ErrorOutline,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF26131F)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = HangisTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = HangisTextSecondary,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HangisCyan,
                        contentColor = Color(0xFF00222A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

enum class EmptyStateType {
    WATCHLIST,
    SEARCH,
    HISTORY,
    NOTIFICATIONS,
    SUBSCRIPTION,
    CATEGORY,
    NETWORK
}

@Composable
fun HangisEmptyState(
    type: EmptyStateType,
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val (icon, title, defaultMessage) = when (type) {
        EmptyStateType.WATCHLIST -> Triple(
            Icons.Default.BookmarkBorder,
            "Your Watchlist is Empty",
            "Explore movies and TV shows to build your personal streaming library."
        )
        EmptyStateType.SEARCH -> Triple(
            Icons.Default.SearchOff,
            "No Results Found",
            customMessage ?: "Try searching for another movie title, actor, or genre."
        )
        EmptyStateType.HISTORY -> Triple(
            Icons.Default.History,
            "No Watch History Yet",
            "Start watching any movie or episode, and you'll be able to resume right where you left off."
        )
        EmptyStateType.NOTIFICATIONS -> Triple(
            Icons.Default.NotificationsNone,
            "No Notifications",
            "You are all caught up! New release announcements and account updates will appear here."
        )
        EmptyStateType.SUBSCRIPTION -> Triple(
            Icons.Default.Subscriptions,
            "No Active Subscription",
            "Upgrade to Hangis Premium to unlock 4K Ultra HD streaming and the entire exclusive catalog."
        )
        EmptyStateType.CATEGORY -> Triple(
            Icons.Default.Movie,
            "No Content in this Category",
            "Check back soon or browse other trending categories."
        )
        EmptyStateType.NETWORK -> Triple(
            Icons.Default.WifiOff,
            "Network Unavailable",
            "Please check your internet connection and try refreshing."
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                HangisCyan.copy(alpha = 0.15f),
                                HangisPurple.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HangisCyan,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = HangisTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = customMessage ?: defaultMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = HangisTextSecondary,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HangisCyan,
                        contentColor = Color(0xFF00222A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

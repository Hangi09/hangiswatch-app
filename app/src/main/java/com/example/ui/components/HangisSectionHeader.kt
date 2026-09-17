package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HangisCyan
import com.example.ui.theme.HangisTextPrimary
import com.example.ui.theme.HangisTextSecondary

@Composable
fun HangisSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    seeAllText: String? = null,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = HangisTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HangisTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (seeAllText != null && onSeeAllClick != null) {
            Text(
                text = seeAllText,
                style = MaterialTheme.typography.labelLarge,
                color = HangisCyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable(onClick = onSeeAllClick)
                    .padding(4.dp)
            )
        }
    }
}

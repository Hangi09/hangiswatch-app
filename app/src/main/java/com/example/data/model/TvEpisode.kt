package com.example.data.model

data class TvEpisode(
    val id: String,
    val tvShowId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val description: String,
    val videoUrl: String,
    val durationMinutes: Int,
    val isPublished: Boolean = true
)

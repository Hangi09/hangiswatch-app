package com.example.data.model

data class Movie(
    val id: String,
    val title: String,
    val description: String,
    val posterUrl: String,
    val backdropUrl: String,
    val trailerUrl: String,
    val videoUrl: String,
    val releaseYear: Int,
    val genre: String,
    val durationMinutes: Int,
    val language: String = "English",
    val subtitles: String = "English, Spanish, French",
    val rating: Double = 8.5,
    val cast: String = "Starring Leading Cast",
    val director: String = "Director",
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isTvShow: Boolean = false,
    val isPublished: Boolean = true,
    val viewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

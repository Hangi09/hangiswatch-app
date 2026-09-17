package com.example.data.repository

import com.example.data.local.TvEpisodeDao
import com.example.data.local.toEntity
import com.example.data.model.TvEpisode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TvShowRepository(
    private val tvEpisodeDao: TvEpisodeDao
) {
    fun getEpisodesBySeason(tvShowId: String, season: Int): Flow<List<TvEpisode>> =
        tvEpisodeDao.getEpisodesBySeason(tvShowId, season).map { list ->
            list.map { it.toTvEpisode() }
        }

    fun getAllEpisodesForShow(tvShowId: String): Flow<List<TvEpisode>> =
        tvEpisodeDao.getAllEpisodesForShow(tvShowId).map { list ->
            list.map { it.toTvEpisode() }
        }

    suspend fun addEpisode(episode: TvEpisode): String {
        val id = if (episode.id.isBlank()) "ep_" + UUID.randomUUID().toString().take(10) else episode.id
        tvEpisodeDao.insertEpisode(episode.copy(id = id).toEntity())
        return id
    }

    suspend fun deleteEpisode(id: String) {
        tvEpisodeDao.deleteEpisodeById(id)
    }
}

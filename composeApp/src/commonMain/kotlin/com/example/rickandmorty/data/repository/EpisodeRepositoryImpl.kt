package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.repository.EpisodeRepository

/**
 * Implementation of the [EpisodeRepository] interface.
 * This class is responsible for fetching episode data from the remote service.
 *
 * @property apiService The remote service for fetching episode data.
 */
class EpisodeRepositoryImpl(private val apiService: ApiService) : EpisodeRepository {
    /**
     * Retrieves a list of episodes from the remote service.
     *
     * @return A list of [Episode] objects.
     */
    override suspend fun getEpisodes(): List<Episode> = apiService.getEpisodes()
}

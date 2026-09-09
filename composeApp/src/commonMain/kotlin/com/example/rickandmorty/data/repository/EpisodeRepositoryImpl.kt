package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.EpisodeRepository

/**
 * Implementation of the [EpisodeRepository] interface.
 * This class is responsible for fetching episode data from the remote service.
 *
 * @property apiService The remote service for fetching episode data.
 */
class EpisodeRepositoryImpl(private val apiService: ApiService) : EpisodeRepository {
    /**
     * Retrieves a page of episodes from the remote service.
     *
     * @param page The page number to retrieve.
     * @return A [Page] of [Episode] objects.
     */
    override suspend fun getEpisodes(page: Int): Page<Episode> = apiService.getEpisodes(page)
}

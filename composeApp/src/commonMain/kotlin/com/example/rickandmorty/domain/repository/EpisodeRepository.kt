package com.example.rickandmorty.domain.repository

import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Page

/**
 * Defines the contract for accessing episode data.
 */
interface EpisodeRepository {
    /**
     * Retrieves a page of episodes.
     *
     * @param page The page number to retrieve.
     * @return A [Page] of [Episode] objects.
     */
    suspend fun getEpisodes(page: Int): Page<Episode>
}

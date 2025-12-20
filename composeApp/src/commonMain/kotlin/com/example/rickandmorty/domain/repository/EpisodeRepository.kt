package com.example.rickandmorty.domain.repository

import com.example.rickandmorty.domain.model.Episode

/**
 * Defines the contract for accessing episode data.
 */
interface EpisodeRepository {
    /**
     * Retrieves a list of episodes.
     *
     * @return A list of [Episode] objects.
     */
    suspend fun getEpisodes(): List<Episode>
}

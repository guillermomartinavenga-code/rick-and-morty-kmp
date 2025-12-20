package com.example.rickandmorty.domain.use_case

import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.repository.EpisodeRepository

/**
 * Use case for retrieving a list of episodes.
 * This class abstracts the logic for fetching episodes from the repository.
 *
 * @property repository The repository for accessing episode data.
 */
class GetEpisodesUseCase(private val repository: EpisodeRepository) {
    /**
     * Executes the use case to get a list of episodes.
     *
     * @return A list of [Episode] objects.
     * @throws Exception if the network request fails.
     */
    suspend operator fun invoke(): List<Episode> = repository.getEpisodes()
}

package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.EpisodeRepository
import com.example.rickandmorty.domain.use_case.api.GetEpisodesUseCase
import com.example.rickandmorty.utils.log.Logger
import com.example.rickandmorty.utils.log.createLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the [GetEpisodesUseCase] interface.
 * This class abstracts the logic for fetching episodes from the repository.
 *
 * @property repository The repository for accessing episode data.
 */
class GetEpisodesUseCaseImpl(
    private val repository: EpisodeRepository,
    private val logger: Logger = createLogger("GetEpisodesUseCase")
) : GetEpisodesUseCase {
    override fun invoke(page: Int): Flow<Lce<Page<Episode>>> = flow {
        logger.debug("Fetching episodes for page $page")
        emit(Lce.Loading)
        val result = repository.getEpisodes(page)
        logger.info("Loaded ${result.items.size} episodes for page $page (hasNextPage=${result.hasNextPage})")
        emit(Lce.Content(result))
    }.catch {
        logger.error("Failed to load episodes for page $page", it)
        emit(Lce.Error(it))
    }
}

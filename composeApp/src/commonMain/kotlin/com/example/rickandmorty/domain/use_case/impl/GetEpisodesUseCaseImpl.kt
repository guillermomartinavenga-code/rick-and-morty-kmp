package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.EpisodeRepository
import com.example.rickandmorty.domain.use_case.api.GetEpisodesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the [GetEpisodesUseCase] interface.
 * This class abstracts the logic for fetching episodes from the repository.
 *
 * @property repository The repository for accessing episode data.
 */
class GetEpisodesUseCaseImpl(private val repository: EpisodeRepository) : GetEpisodesUseCase {
    override fun invoke(page: Int): Flow<Lce<Page<Episode>>> = flow {
        emit(Lce.Loading)
        emit(Lce.Content(repository.getEpisodes(page)))
    }.catch { emit(Lce.Error(it)) }
}

package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.CharacterRepository
import com.example.rickandmorty.domain.use_case.api.GetCharactersUseCase
import com.example.rickandmorty.utils.log.Logger
import com.example.rickandmorty.utils.log.createLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the [GetCharactersUseCase] interface.
 * This class abstracts the logic for fetching characters from the repository.
 *
 * @property repository The repository for accessing character data.
 */
class GetCharactersUseCaseImpl(
    private val repository: CharacterRepository,
    private val logger: Logger = createLogger("GetCharactersUseCase")
) : GetCharactersUseCase {
    override fun invoke(page: Int): Flow<Lce<Page<Character>>> = flow {
        logger.debug("Fetching characters for page $page")
        emit(Lce.Loading)
        val result = repository.getCharacters(page)
        logger.info("Loaded ${result.items.size} characters for page $page (hasNextPage=${result.hasNextPage})")
        emit(Lce.Content(result))
    }.catch {
        logger.error("Failed to load characters for page $page", it)
        emit(Lce.Error(it))
    }
}

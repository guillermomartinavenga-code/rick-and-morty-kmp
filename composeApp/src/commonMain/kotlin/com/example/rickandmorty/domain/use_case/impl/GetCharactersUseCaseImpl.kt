package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.CharacterRepository
import com.example.rickandmorty.domain.use_case.api.GetCharactersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the [GetCharactersUseCase] interface.
 * This class abstracts the logic for fetching characters from the repository.
 *
 * @property repository The repository for accessing character data.
 */
class GetCharactersUseCaseImpl(private val repository: CharacterRepository) : GetCharactersUseCase {
    override fun invoke(page: Int): Flow<Lce<Page<Character>>> = flow {
        emit(Lce.Loading)
        emit(Lce.Content(repository.getCharacters(page)))
    }.catch { emit(Lce.Error(it)) }
}

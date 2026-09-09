package com.example.rickandmorty.domain.use_case.api

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Page
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving a page of characters.
 */
interface GetCharactersUseCase {
    /**
     * Executes the use case to get a page of characters.
     *
     * @param page The page number to retrieve.
     * @return A [Flow] emitting [Lce.Loading], then either [Lce.Content] with a
     * [Page] of [Character] objects or [Lce.Error] if the network request fails.
     */
    operator fun invoke(page: Int): Flow<Lce<Page<Character>>>
}

package com.example.rickandmorty.domain.repository

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Page

/**
 * Defines the contract for accessing character data.
 * This interface will be implemented in the data layer.
 */
interface CharacterRepository {
    /**
     * Retrieves a page of characters.
     *
     * @param page The page number to retrieve.
     * @return A [Page] of [Character] objects.
     */
    suspend fun getCharacters(page: Int): Page<Character>
}

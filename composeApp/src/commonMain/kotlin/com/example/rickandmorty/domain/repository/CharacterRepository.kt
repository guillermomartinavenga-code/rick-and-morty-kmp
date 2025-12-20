package com.example.rickandmorty.domain.repository

import com.example.rickandmorty.domain.model.Character

/**
 * Defines the contract for accessing character data.
 * This interface will be implemented in the data layer.
 */
interface CharacterRepository {
    /**
     * Retrieves a list of characters.
     *
     * @return A list of [Character] objects.
     */
    suspend fun getCharacters(): List<Character>
}

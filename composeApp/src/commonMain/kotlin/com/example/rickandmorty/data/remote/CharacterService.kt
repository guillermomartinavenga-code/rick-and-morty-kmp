package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character

/**
 * Defines the expected network service for fetching character data.
 * This expect class will have actual implementations for Android and JVM platforms.
 */
expect class CharacterService {
    /**
     * Retrieves a list of characters from the API.
     *
     * @return A list of [Character] objects.
     */
    suspend fun getCharacters(): List<Character>
}

/**
 * Creates an instance of [CharacterService] for the target platform.
 */
expect fun createCharacterService(): CharacterService

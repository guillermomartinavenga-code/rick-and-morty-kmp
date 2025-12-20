package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.repository.CharacterRepository

/**
 * Implementation of the [CharacterRepository] interface.
 * This class is responsible for fetching character data from the remote service.
 *
 * @property apiService The remote service for fetching character data.
 */
class CharacterRepositoryImpl(private val apiService: ApiService) : CharacterRepository {
    /**
     * Retrieves a list of characters from the remote service.
     *
     * @return A list of [Character] objects.
     */
    override suspend fun getCharacters(): List<Character> = apiService.getCharacters()
}

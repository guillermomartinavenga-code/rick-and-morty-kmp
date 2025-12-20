package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [CharacterRepositoryImpl].
 */
class CharacterRepositoryImplTest {

    @Test
    fun `getCharacters should return characters from apiService`() = runBlocking {
        // Given
        val characters = listOf(
            Character(1, "Rick Sanchez", "Human", "Male", "Earth (C-137)", "Citadel of Ricks", "image_url")
        )
        val mockApiService = object : ApiService {
            override suspend fun getCharacters(): List<Character> = characters
            override suspend fun getLocations(): List<Location> = emptyList()
            override suspend fun getEpisodes(): List<Episode> = emptyList()
        }
        val repository = CharacterRepositoryImpl(mockApiService)

        // When
        val result = repository.getCharacters()

        // Then
        assertEquals(characters, result)
    }
}

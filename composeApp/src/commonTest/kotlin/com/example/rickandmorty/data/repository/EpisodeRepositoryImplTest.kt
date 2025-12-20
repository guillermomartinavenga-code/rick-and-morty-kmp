package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [EpisodeRepositoryImpl].
 */
class EpisodeRepositoryImplTest {

    @Test
    fun `getEpisodes should return episodes from apiService`() = runBlocking {
        // Given
        val episodes = listOf(
            Episode(1, "Pilot", "December 2, 2013", "S01E01")
        )
        val mockApiService = object : ApiService {
            override suspend fun getCharacters(): List<Character> = emptyList()
            override suspend fun getLocations(): List<Location> = emptyList()
            override suspend fun getEpisodes(): List<Episode> = episodes
        }
        val repository = EpisodeRepositoryImpl(mockApiService)

        // When
        val result = repository.getEpisodes()

        // Then
        assertEquals(episodes, result)
    }
}

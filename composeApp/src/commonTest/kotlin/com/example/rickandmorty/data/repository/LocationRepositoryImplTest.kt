package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [LocationRepositoryImpl].
 */
class LocationRepositoryImplTest {

    @Test
    fun `getLocations should return locations from apiService`() = runBlocking {
        // Given
        val locations = listOf(
            Location(1, "Earth (C-137)", "Planet", "Dimension C-137")
        )
        val mockApiService = object : ApiService {
            override suspend fun getCharacters(): List<Character> = emptyList()
            override suspend fun getLocations(): List<Location> = locations
            override suspend fun getEpisodes(): List<Episode> = emptyList()
        }
        val repository = LocationRepositoryImpl(mockApiService)

        // When
        val result = repository.getLocations()

        // Then
        assertEquals(locations, result)
    }
}

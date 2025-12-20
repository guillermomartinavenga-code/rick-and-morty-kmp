package com.example.rickandmorty.domain.use_case

import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.repository.LocationRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [GetLocationsUseCase].
 */
class GetLocationsUseCaseTest {

    @Test
    fun `invoke should return locations from repository`() = runBlocking {
        // Given
        val locations = listOf(
            Location(1, "Earth (C-137)", "Planet", "Dimension C-137"),
            Location(2, "Citadel of Ricks", "Space station", "Unknown")
        )
        val mockRepository = object : LocationRepository {
            override suspend fun getLocations(): List<Location> = locations
        }
        val useCase = GetLocationsUseCase(mockRepository)

        // When
        val result = useCase()

        // Then
        assertEquals(locations, result)
    }
}

package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.testutil.defaultLocation
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [LocationRepositoryImpl].
 */
class LocationRepositoryImplTest {

    @MockK
    lateinit var mockApiService: ApiService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getLocations should return locations from apiService using fake`() = runTest {
        // Given
        val expectedPage = Page(items = listOf(defaultLocation), hasNextPage = true)

        val fakeApiService = FakeApiService(
            locationsPage = expectedPage
        )
        val repository = LocationRepositoryImpl(fakeApiService)

        // When
        val result = repository.getLocations(1)

        // Then
        assertEquals(
            expectedPage,
            result
        )
    }

    @Test
    fun `getLocations should return locations from apiService using mock`() = runTest {
        // Given
        val expectedPage = Page(items = listOf(defaultLocation), hasNextPage = true)

        coEvery { mockApiService.getLocations(1) }.returns(
            expectedPage
        )

        val repository = LocationRepositoryImpl(mockApiService)

        // When
        val result = repository.getLocations(1)

        // Then
        assertEquals(
            expectedPage,
            result
        )
    }
}

package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.testutil.defaultEpisode
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [EpisodeRepositoryImpl].
 */
class EpisodeRepositoryImplTest {
    @MockK
    lateinit var mockApiService: ApiService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getEpisodes should return episodes from apiService using fake`() = runTest {
        // Given
        val expectedPage = Page(items = listOf(defaultEpisode), hasNextPage = true)
        val fakeApiService = FakeApiService(episodesPage = expectedPage)
        val repository = EpisodeRepositoryImpl(fakeApiService)

        // When
        val result = repository.getEpisodes(1)

        // Then
        assertEquals(expectedPage, result)
    }

    @Test
    fun `getEpisodes should return episodes from apiService using mock`() = runTest {
        // Given
        val expectedPage = Page(items = listOf(defaultEpisode), hasNextPage = true)

        coEvery { mockApiService.getEpisodes(1) }.returns(
            expectedPage
        )

        val repository = EpisodeRepositoryImpl(mockApiService)

        // When
        val result = repository.getEpisodes(1)

        // Then
        assertEquals(
            expectedPage,
            result
        )
    }
}

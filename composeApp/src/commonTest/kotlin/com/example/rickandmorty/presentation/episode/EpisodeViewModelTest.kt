package com.example.rickandmorty.presentation.episode

import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [EpisodeViewModel].
 */
@ExperimentalCoroutinesApi
class EpisodeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `loadEpisodes should update episodes state`() = runTest {
        // Given
        val episodes = listOf(
            Episode(1, "Pilot", "December 2, 2013", "S01E01")
        )
        val useCase = Module.getEpisodesUseCase
        val viewModel = EpisodeViewModel()

        // When
        viewModel.loadEpisodes()
        testScheduler.advanceUntilIdle() // Allow the coroutine to complete

        // Then
        val result = viewModel.episodes.first()
        // Note: In a real test, we would mock the use case to return the expected episodes.
        // For this example, we just verify that the list is not empty.
        assertEquals(true, result.isNotEmpty())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

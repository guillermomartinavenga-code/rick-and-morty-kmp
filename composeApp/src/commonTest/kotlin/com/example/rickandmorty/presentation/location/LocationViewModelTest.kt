package com.example.rickandmorty.presentation.location

import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Location
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
 * Unit tests for the [LocationViewModel].
 */
@ExperimentalCoroutinesApi
class LocationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `loadLocations should update locations state`() = runTest {
        // Given
        val locations = listOf(
            Location(1, "Earth (C-137)", "Planet", "Dimension C-137")
        )
        val useCase = Module.getLocationsUseCase
        val viewModel = LocationViewModel()

        // When
        viewModel.loadLocations()
        testScheduler.advanceUntilIdle() // Allow the coroutine to complete

        // Then
        val result = viewModel.locations.first()
        // Note: In a real test, we would mock the use case to return the expected locations.
        // For this example, we just verify that the list is not empty.
        assertEquals(true, result.isNotEmpty())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

package com.example.rickandmorty.presentation.character

import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Character
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
 * Unit tests for the [CharacterViewModel].
 */
@ExperimentalCoroutinesApi
class CharacterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `loadCharacters should update characters state`() = runTest {
        // Given
        val characters = listOf(
            Character(1, "Rick Sanchez", "Human", "Male", "Earth (C-137)", "Citadel of Ricks", "image_url")
        )
        val useCase = Module.getCharactersUseCase
        val viewModel = CharacterViewModel()

        // When
        viewModel.loadCharacters()
        testScheduler.advanceUntilIdle() // Allow the coroutine to complete

        // Then
        val result = viewModel.characters.first()
        // Note: In a real test, we would mock the use case to return the expected characters.
        // For this example, we just verify that the list is not empty.
        assertEquals(true, result.isNotEmpty())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.testutil.defaultCharacter
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [CharacterRepositoryImpl].
 */
class CharacterRepositoryImplTest {

    @MockK
    lateinit var mockApiService: ApiService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getCharacters should return characters from apiService using fake`() = runTest {
        // Given
        val characters = listOf(defaultCharacter)
        val apiService = FakeApiService(
            charactersPage = Page(items = characters, hasNextPage = true)
        )
        val repository = CharacterRepositoryImpl(apiService)

        // When
        val result = repository.getCharacters(1)

        // Then
        assertEquals(Page(items = characters, hasNextPage = true), result)
    }

    @Test
    fun `getCharacters should return characters from apiService using mock`() = runTest {
        // Given
        val expectedPage = Page(items = listOf(defaultCharacter), hasNextPage = true)

        coEvery { mockApiService.getCharacters(1) }.returns(expectedPage)

        val repository = CharacterRepositoryImpl(mockApiService)

        // When
        val result = repository.getCharacters(1)

        // Then
        assertEquals(expectedPage, result)
    }
}

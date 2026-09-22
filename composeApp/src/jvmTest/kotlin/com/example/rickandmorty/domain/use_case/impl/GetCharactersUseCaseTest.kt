package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.repository.CharacterRepository
import com.example.rickandmorty.testutil.defaultCharacter
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for the [com.example.rickandmorty.domain.use_case.api.GetCharactersUseCase].
 */
class GetCharactersUseCaseTest {

    @MockK
    lateinit var mockRepository: CharacterRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke should emit Loading then Success with the repository page using fake`() = runTest {
        // Given
        val characters = listOf(
            defaultCharacter,
            defaultCharacter.copy(id = 2, name = "Morty Smith")
        )

        val expectedPage = Page(items = characters, hasNextPage = true)

        val fakeRepository = object : CharacterRepository {
            override suspend fun getCharacters(page: Int): Page<Character> = expectedPage
        }

        val useCase = GetCharactersUseCaseImpl(fakeRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(listOf(Lce.Loading, Lce.Content(expectedPage)), emissions)
    }

    @Test
    fun `invoke should emit Loading then Success with the repository page using mock`() = runTest {
        // Given
        val characters = listOf(
            defaultCharacter,
            defaultCharacter.copy(id = 2, name = "Morty Smith")
        )

        val expectedPage = Page(items = characters, hasNextPage = true)

        coEvery { mockRepository.getCharacters(1) }.returns(expectedPage)

        val useCase = GetCharactersUseCaseImpl(mockRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(listOf(Lce.Loading, Lce.Content(expectedPage)), emissions)
        coVerify(exactly = 1) { mockRepository.getCharacters(1) }
    }

    @Test
    fun `invoke should emit Loading then Error when the repository throws`() = runTest {
        // Given
        val exception = RuntimeException("boom")
        coEvery { mockRepository.getCharacters(1) } throws exception

        val useCase = GetCharactersUseCaseImpl(mockRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(Lce.Loading, emissions.first())
        val error = assertIs<Lce.Error>(emissions[1])
        assertEquals(exception, error.throwable)
        coVerify(exactly = 1) { mockRepository.getCharacters(1) }
    }
}

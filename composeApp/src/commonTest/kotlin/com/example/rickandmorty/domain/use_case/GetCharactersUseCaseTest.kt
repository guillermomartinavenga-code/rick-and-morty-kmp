package com.example.rickandmorty.domain.use_case

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.repository.CharacterRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [GetCharactersUseCase].
 */
class GetCharactersUseCaseTest {

    @Test
    fun `invoke should return characters from repository`() = runBlocking {
        // Given
        val characters = listOf(
            Character(1, "Rick Sanchez", "Human", "Male", "Earth (C-137)", "Citadel of Ricks", "image_url"),
            Character(2, "Morty Smith", "Human", "Male", "Earth (C-137)", "Citadel of Ricks", "image_url")
        )
        val mockRepository = object : CharacterRepository {
            override suspend fun getCharacters(): List<Character> = characters
        }
        val useCase = GetCharactersUseCase(mockRepository)

        // When
        val result = useCase()

        // Then
        assertEquals(characters, result)
    }
}

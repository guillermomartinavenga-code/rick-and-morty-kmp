package com.example.rickandmorty.domain.use_case

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.repository.CharacterRepository

/**
 * Use case for retrieving a list of characters.
 * This class abstracts the logic for fetching characters from the repository.
 *
 * @property repository The repository for accessing character data.
 */
class GetCharactersUseCase(private val repository: CharacterRepository) {
    /**
     * Executes the use case to get a list of characters.
     *
     * @return A list of [Character] objects.
     * @throws Exception if the network request fails.
     */
    suspend operator fun invoke(): List<Character> = repository.getCharacters()
}

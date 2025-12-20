package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Actual implementation of the network service for the JVM platform using Ktor.
 */
actual class CharacterService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    /**
     * Retrieves a list of characters from the API.
     *
     * @return A list of [Character] objects.
     */
    actual suspend fun getCharacters(): List<Character> {
        val response: CharacterResponse = client.get("https://rickandmortyapi.com/api/character").body()
        return response.results.map { characterDto ->
            Character(
                id = characterDto.id,
                name = characterDto.name,
                species = characterDto.species,
                gender = characterDto.gender,
                origin = characterDto.origin.name,
                location = characterDto.location.name,
                image = characterDto.image
            )
        }
    }
}

/**
 * Creates an instance of [CharacterService] for the JVM platform.
 */
actual fun createCharacterService(): CharacterService = CharacterService()

package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Actual implementation of the network service for the Android platform using Retrofit.
 */
actual class CharacterService {

    private interface ApiService {
        @GET("character")
        suspend fun getCharacters(): CharacterResponse
    }

    private val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Retrieves a list of characters from the API.
     *
     * @return A list of [Character] objects.
     */
    actual suspend fun getCharacters(): List<Character> {
        return service.getCharacters().results.map { characterDto ->
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
 * Creates an instance of [CharacterService] for the Android platform.
 */
actual fun createCharacterService(): CharacterService = CharacterService()

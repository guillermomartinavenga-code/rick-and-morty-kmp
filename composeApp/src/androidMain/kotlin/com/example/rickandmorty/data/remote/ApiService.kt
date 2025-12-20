package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Android implementation of the [ApiService] interface using Retrofit.
 */
class ApiServiceImpl : ApiService {

    private interface RetrofitApiService {
        @GET("character")
        suspend fun getCharacters(): CharacterResponse

        @GET("location")
        suspend fun getLocations(): LocationResponse

        @GET("episode")
        suspend fun getEpisodes(): EpisodeResponse
    }

    private val service: RetrofitApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitApiService::class.java)
    }

    override suspend fun getCharacters(): List<Character> {
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

    override suspend fun getLocations(): List<Location> {
        return service.getLocations().results.map { locationDto ->
            Location(
                id = locationDto.id,
                name = locationDto.name,
                type = locationDto.type,
                dimension = locationDto.dimension
            )
        }
    }

    override suspend fun getEpisodes(): List<Episode> {
        return service.getEpisodes().results.map { episodeDto ->
            Episode(
                id = episodeDto.id,
                name = episodeDto.name,
                airDate = episodeDto.air_date,
                episode = episodeDto.episode
            )
        }
    }
}

/**
 * Creates an instance of [ApiService] for the Android platform.
 */
actual fun createApiService(): ApiService = ApiServiceImpl()

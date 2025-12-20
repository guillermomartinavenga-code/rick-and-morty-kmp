package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * JVM implementation of the [ApiService] interface using Ktor.
 */
class ApiServiceImpl : ApiService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    override suspend fun getCharacters(): List<Character> {
        return try {
            val response: CharacterResponse = client.get("https://rickandmortyapi.com/api/character").body()
            response.results.map { characterDto ->
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
        } catch (e: Exception) {
            println("Error fetching characters: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getLocations(): List<Location> {
        return try {
            val response: LocationResponse = client.get("https://rickandmortyapi.com/api/location").body()
            response.results.map { locationDto ->
                Location(
                    id = locationDto.id,
                    name = locationDto.name,
                    type = locationDto.type,
                    dimension = locationDto.dimension
                )
            }
        } catch (e: Exception) {
            println("Error fetching locations: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getEpisodes(): List<Episode> {
        return try {
            val response: EpisodeResponse = client.get("https://rickandmortyapi.com/api/episode").body()
            response.results.map { episodeDto ->
                Episode(
                    id = episodeDto.id,
                    name = episodeDto.name,
                    airDate = episodeDto.air_date,
                    episode = episodeDto.episode
                )
            }
        } catch (e: Exception) {
            println("Error fetching episodes: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}

/**
 * Creates an instance of [ApiService] for the JVM platform.
 */
actual fun createApiService(): ApiService = ApiServiceImpl()

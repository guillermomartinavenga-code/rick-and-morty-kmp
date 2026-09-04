package com.example.rickandmorty.di

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.data.remote.ApiServiceImpl
import com.example.rickandmorty.data.remote.kmpgen.Api
import com.example.rickandmorty.data.repository.CharacterRepositoryImpl
import com.example.rickandmorty.data.repository.EpisodeRepositoryImpl
import com.example.rickandmorty.data.repository.LocationRepositoryImpl
import com.example.rickandmorty.domain.repository.CharacterRepository
import com.example.rickandmorty.domain.repository.EpisodeRepository
import com.example.rickandmorty.domain.repository.LocationRepository
import com.example.rickandmorty.domain.use_case.GetCharactersUseCase
import com.example.rickandmorty.domain.use_case.GetEpisodesUseCase
import com.example.rickandmorty.domain.use_case.GetLocationsUseCase
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel.INFO
import io.ktor.client.plugins.HttpTimeout

/**
 * A simple dependency injection module to provide instances of the required classes.
 */
object Module {

    init {
        // Configura el cliente HTTP de Api (singleton generado) antes de que
        // cualquier ApiXxx lo use. Se ejecuta una única vez, al cargar Module.
        Api.updateClient(
            decorator = {
                install(HttpTimeout) {
                    requestTimeoutMillis = 15_000
                    connectTimeoutMillis = 15_000
                    socketTimeoutMillis = 15_000
                }
                install(Logging) {
                    level = INFO
                }
            }
        )
    }
    private val apiService: ApiService by lazy {
        ApiServiceImpl()
    }

    private val characterRepository: CharacterRepository by lazy {
        CharacterRepositoryImpl(apiService)
    }

    private val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(apiService)
    }

    private val episodeRepository: EpisodeRepository by lazy {
        EpisodeRepositoryImpl(apiService)
    }

    val getCharactersUseCase: GetCharactersUseCase by lazy {
        GetCharactersUseCase(characterRepository)
    }

    val getLocationsUseCase: GetLocationsUseCase by lazy {
        GetLocationsUseCase(locationRepository)
    }

    val getEpisodesUseCase: GetEpisodesUseCase by lazy {
        GetEpisodesUseCase(episodeRepository)
    }
}

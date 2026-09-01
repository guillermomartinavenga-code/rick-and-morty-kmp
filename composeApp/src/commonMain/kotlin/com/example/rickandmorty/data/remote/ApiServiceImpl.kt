package com.example.rickandmorty.data.remote

import com.example.rickandmorty.client.Client
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location

/**
 * Implementación única de [ApiService] para todas las plataformas.
 * El [HttpClient] recibido ya trae configurado el baseUrl, JSON y logging
 * (ver HttpClientFactory), por lo que acá solo se definen los paths relativos.
 */
class ApiServiceImpl(private val client: Client) : ApiService {

    override suspend fun getCharacters(): List<Character> =
        client.getCharacter().dataOrThrow().results.map { it.toDomain() }

    override suspend fun getLocations(): List<Location> =
        client.getLocation().dataOrThrow().results.map { it.toDomain() }

    override suspend fun getEpisodes(): List<Episode> =
        client.getEpisode().dataOrThrow().results.map { it.toDomain() }
}
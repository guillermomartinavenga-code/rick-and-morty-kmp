package com.example.rickandmorty.data.remote

import com.example.rickandmorty.data.remote.kmpgen.api.CharacterApi
import com.example.rickandmorty.data.remote.kmpgen.api.LocationApi
import com.example.rickandmorty.data.remote.kmpgen.api.EpisodeApi
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location

class ApiServiceImpl : ApiService {

    override suspend fun getCharacters(): List<Character> =
        CharacterApi.getAllCharacters().fold(
            ifLeft = { throw it },
            ifRight = { response -> response.data.results.orEmpty().map { it.toDomain() } }
        )

    override suspend fun getLocations(): List<Location> =
        LocationApi.getAllLocations().fold(
            ifLeft = { throw it },
            ifRight = { response -> response.data.results.orEmpty().map { it.toDomain() } }
        )

    override suspend fun getEpisodes(): List<Episode> =
        EpisodeApi.getAllEpisodes().fold(
            ifLeft = { throw it },
            ifRight = { response -> response.data.results.orEmpty().map { it.toDomain() } }
        )
}
package com.example.rickandmorty.data.remote

import com.example.rickandmorty.data.remote.kmpgen.api.CharacterApi
import com.example.rickandmorty.data.remote.kmpgen.api.LocationApi
import com.example.rickandmorty.data.remote.kmpgen.api.EpisodeApi
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page

class ApiServiceImpl : ApiService {

    override suspend fun getCharacters(page: Int): Page<Character> =
        CharacterApi.getAllCharacters(page = page.toLong()).fold(
            ifLeft = { throw it },
            ifRight = { response -> response.data.toDomainPage() }
        )

    override suspend fun getLocations(page: Int): Page<Location> =
        LocationApi.getAllLocations(page = page.toLong()).fold(
            ifLeft = { throw it },
            ifRight = { response -> response.data.toDomainPage() }
        )

    override suspend fun getEpisodes(page: Int): Page<Episode> =
        EpisodeApi.getAllEpisodes(page = page.toLong()).fold(
            ifLeft = { throw it },
            ifRight = { response -> response.data.toDomainPage() }
        )
}

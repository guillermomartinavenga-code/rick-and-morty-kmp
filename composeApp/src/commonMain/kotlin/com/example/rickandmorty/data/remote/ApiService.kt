package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location

/**
 * Defines the contract for the network service for fetching data.
 */
interface ApiService {
    /**
     * Retrieves a list of characters.
     */
    suspend fun getCharacters(): List<Character>

    /**
     * Retrieves a list of locations.
     */
    suspend fun getLocations(): List<Location>

    /**
     * Retrieves a list of episodes.
     */
    suspend fun getEpisodes(): List<Episode>
}
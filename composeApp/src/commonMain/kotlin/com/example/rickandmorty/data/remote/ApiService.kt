package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page

/**
 * Defines the contract for the network service for fetching data.
 */
interface ApiService {
    /**
     * Retrieves a page of characters.
     */
    suspend fun getCharacters(page: Int): Page<Character>

    /**
     * Retrieves a page of locations.
     */
    suspend fun getLocations(page: Int): Page<Location>

    /**
     * Retrieves a page of episodes.
     */
    suspend fun getEpisodes(page: Int): Page<Episode>
}

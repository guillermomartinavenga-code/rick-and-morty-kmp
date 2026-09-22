package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page

/**
 * Test double for [ApiService] shared by the repository tests.
 * Each page defaults to empty so a test only needs to configure the one it cares about.
 */
class FakeApiService(
    private val charactersPage: Page<Character> = Page(items = emptyList(), hasNextPage = false),
    private val locationsPage: Page<Location> = Page(items = emptyList(), hasNextPage = false),
    private val episodesPage: Page<Episode> = Page(items = emptyList(), hasNextPage = false)
) : ApiService {
    override suspend fun getCharacters(page: Int): Page<Character> = charactersPage
    override suspend fun getLocations(page: Int): Page<Location> = locationsPage
    override suspend fun getEpisodes(page: Int): Page<Episode> = episodesPage
}

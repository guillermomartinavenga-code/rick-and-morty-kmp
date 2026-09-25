package com.example.rickandmorty.domain.repository

import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page

/**
 * Defines the contract for accessing location data.
 */
interface LocationRepository {
    /**
     * Retrieves a page of locations.
     *
     * @param page The page number to retrieve.
     * @return A [Page] of [Location] objects.
     */
    suspend fun getLocations(page: Int): Page<Location>
}

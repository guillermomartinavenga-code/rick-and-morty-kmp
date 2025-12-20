package com.example.rickandmorty.domain.repository

import com.example.rickandmorty.domain.model.Location

/**
 * Defines the contract for accessing location data.
 */
interface LocationRepository {
    /**
     * Retrieves a list of locations.
     *
     * @return A list of [Location] objects.
     */
    suspend fun getLocations(): List<Location>
}

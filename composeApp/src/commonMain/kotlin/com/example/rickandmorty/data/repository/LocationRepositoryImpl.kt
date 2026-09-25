package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.LocationRepository

/**
 * Implementation of the [LocationRepository] interface.
 * This class is responsible for fetching location data from the remote service.
 *
 * @property apiService The remote service for fetching location data.
 */
class LocationRepositoryImpl(private val apiService: ApiService) : LocationRepository {
    /**
     * Retrieves a page of locations from the remote service.
     *
     * @param page The page number to retrieve.
     * @return A [Page] of [Location] objects.
     */
    override suspend fun getLocations(page: Int): Page<Location> = apiService.getLocations(page)
}

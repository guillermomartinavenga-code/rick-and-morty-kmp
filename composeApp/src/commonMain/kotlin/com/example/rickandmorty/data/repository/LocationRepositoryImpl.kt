package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.ApiService
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.repository.LocationRepository

/**
 * Implementation of the [LocationRepository] interface.
 * This class is responsible for fetching location data from the remote service.
 *
 * @property apiService The remote service for fetching location data.
 */
class LocationRepositoryImpl(private val apiService: ApiService) : LocationRepository {
    /**
     * Retrieves a list of locations from the remote service.
     *
     * @return A list of [Location] objects.
     */
    override suspend fun getLocations(): List<Location> = apiService.getLocations()
}

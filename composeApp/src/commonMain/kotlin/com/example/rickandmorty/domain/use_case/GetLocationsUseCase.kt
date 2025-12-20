package com.example.rickandmorty.domain.use_case

import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.repository.LocationRepository

/**
 * Use case for retrieving a list of locations.
 * This class abstracts the logic for fetching locations from the repository.
 *
 * @property repository The repository for accessing location data.
 */
class GetLocationsUseCase(private val repository: LocationRepository) {
    /**
     * Executes the use case to get a list of locations.
     *
     * @return A list of [Location] objects.
     * @throws Exception if the network request fails.
     */
    suspend operator fun invoke(): List<Location> = repository.getLocations()
}

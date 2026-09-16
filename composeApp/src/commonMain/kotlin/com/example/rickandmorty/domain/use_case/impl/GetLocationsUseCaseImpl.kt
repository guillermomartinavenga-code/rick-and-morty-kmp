package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.LocationRepository
import com.example.rickandmorty.domain.use_case.api.GetLocationsUseCase
import com.example.rickandmorty.utils.log.Logger
import com.example.rickandmorty.utils.log.createLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the [GetLocationsUseCase] interface.
 * This class abstracts the logic for fetching locations from the repository.
 *
 * @property repository The repository for accessing location data.
 */
class GetLocationsUseCaseImpl(
    private val repository: LocationRepository,
    private val logger: Logger = createLogger("GetLocationsUseCase")
) : GetLocationsUseCase {
    override fun invoke(page: Int): Flow<Lce<Page<Location>>> = flow {
        logger.debug("Fetching locations for page $page")
        emit(Lce.Loading)
        val result = repository.getLocations(page)
        logger.info("Loaded ${result.items.size} locations for page $page (hasNextPage=${result.hasNextPage})")
        emit(Lce.Content(result))
    }.catch {
        logger.error("Failed to load locations for page $page", it)
        emit(Lce.Error(it))
    }
}

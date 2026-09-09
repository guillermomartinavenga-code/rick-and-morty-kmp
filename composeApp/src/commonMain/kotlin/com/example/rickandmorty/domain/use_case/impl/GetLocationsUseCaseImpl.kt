package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.LocationRepository
import com.example.rickandmorty.domain.use_case.api.GetLocationsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the [GetLocationsUseCase] interface.
 * This class abstracts the logic for fetching locations from the repository.
 *
 * @property repository The repository for accessing location data.
 */
class GetLocationsUseCaseImpl(private val repository: LocationRepository) : GetLocationsUseCase {
    override fun invoke(page: Int): Flow<Lce<Page<Location>>> = flow {
        emit(Lce.Loading)
        emit(Lce.Content(repository.getLocations(page)))
    }.catch { emit(Lce.Error(it)) }
}

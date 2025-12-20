package com.example.rickandmorty.presentation.location

import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the state and business logic for the location list screen.
 * This class is a plain Kotlin class and does not extend Android's ViewModel,
 * making it fully compatible with Kotlin Multiplatform.
 */
class LocationViewModel {

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    /**
     * A [StateFlow] that emits the current list of locations.
     */
    val locations = _locations.asStateFlow()

    /**
     * Fetches the list of locations from the use case and updates the state.
     * This is a suspend function and should be called from a coroutine.
     */
    suspend fun loadLocations() {
        _locations.value = Module.getLocationsUseCase()
    }
}

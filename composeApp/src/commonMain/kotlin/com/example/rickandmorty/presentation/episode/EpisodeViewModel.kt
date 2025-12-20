package com.example.rickandmorty.presentation.episode

import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Episode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the state and business logic for the episode list screen.
 * This class is a plain Kotlin class and does not extend Android's ViewModel,
 * making it fully compatible with Kotlin Multiplatform.
 */
class EpisodeViewModel {

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    /**
     * A [StateFlow] that emits the current list of episodes.
     */
    val episodes = _episodes.asStateFlow()

    /**
     * Fetches the list of episodes from the use case and updates the state.
     * This is a suspend function and should be called from a coroutine.
     */
    suspend fun loadEpisodes() {
        _episodes.value = Module.getEpisodesUseCase()
    }
}

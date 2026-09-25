package com.example.rickandmorty.presentation.episode

import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.use_case.api.GetEpisodesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Manages the state and business logic for the episode list screen.
 * This class is a plain Kotlin class and does not extend Android's ViewModel,
 * making it fully compatible with Kotlin Multiplatform.
 */
class EpisodeViewModel(
    private val getEpisodesUseCase: GetEpisodesUseCase = Module.getEpisodesUseCase
) {

    private companion object {
        val MIN_LOADING_DURATION = 400.milliseconds
    }

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    /**
     * A [kotlinx.coroutines.flow.StateFlow] that emits the current accumulated list of episodes.
     */
    val episodes = _episodes.asStateFlow()

    private val _isLoadingNextPage = MutableStateFlow(false)
    /**
     * A [kotlinx.coroutines.flow.StateFlow] indicating whether a next page is currently being
     * fetched.
     */
    val isLoadingNextPage = _isLoadingNextPage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    /**
     * A [kotlinx.coroutines.flow.StateFlow] carrying the last error message from a failed page
     * fetch, if any.
     */
    val errorMessage = _errorMessage.asStateFlow()

    private var currentPage = 0
    private var endReached = false

    /**
     * Resets pagination state and fetches the first page of episodes.
     */
    suspend fun loadEpisodes() {
        currentPage = 0
        endReached = false
        _episodes.value = emptyList()
        loadNextPageIfNeeded()
    }

    /**
     * Fetches the next page of episodes and appends it to the current list.
     * No-ops if a page is already being fetched or the last page was already reached.
     */
    suspend fun loadNextPageIfNeeded() {
        if (_isLoadingNextPage.value || endReached) return

        _isLoadingNextPage.value = true
        _errorMessage.value = null
        val nextPage = currentPage + 1
        val startMark = TimeSource.Monotonic.markNow()
        try {
            getEpisodesUseCase(nextPage).collect { lce ->
                when (lce) {
                    Lce.Loading -> Unit
                    is Lce.Content -> {
                        awaitMinimumLoadingDuration(startMark)
                        currentPage = nextPage
                        endReached = !lce.data.hasNextPage
                        _episodes.value += lce.data.items
                    }
                    is Lce.Error -> {
                        awaitMinimumLoadingDuration(startMark)
                        _errorMessage.value = "The next page of episodes could not be loaded."
                    }
                }
            }
        } finally {
            _isLoadingNextPage.value = false
        }
    }

    private suspend fun awaitMinimumLoadingDuration(startMark: TimeSource.Monotonic.ValueTimeMark) {
        val elapsed = startMark.elapsedNow()
        if (elapsed < MIN_LOADING_DURATION) {
            delay(MIN_LOADING_DURATION - elapsed)
        }
    }
}

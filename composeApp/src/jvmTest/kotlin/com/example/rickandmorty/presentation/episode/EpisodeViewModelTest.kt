package com.example.rickandmorty.presentation.episode

import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.use_case.api.GetEpisodesUseCase
import com.example.rickandmorty.testutil.createEpisode
import com.example.rickandmorty.testutil.defaultEpisode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Unit tests for the [EpisodeViewModel].
 */
@ExperimentalCoroutinesApi
class EpisodeViewModelTest {

    @MockK
    lateinit var mockUseCase: GetEpisodesUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadEpisodes should update episodes state using fake`() = runTest {
        // Given
        val expectedEpisodes = listOf(defaultEpisode)
        val expectedPage = Page(items = expectedEpisodes, hasNextPage = true)
        val fakeUseCase = object : GetEpisodesUseCase {
            override fun invoke(page: Int): Flow<Lce<Page<Episode>>> {
                return flowOf(Lce.Content(expectedPage))
            }
        }
        val viewModel = EpisodeViewModel(fakeUseCase)

        // When
        viewModel.loadEpisodes()
        testScheduler.advanceUntilIdle() // Allow the coroutine to complete

        // Then
        val result = viewModel.episodes.first()

        assertEquals(expectedEpisodes, actual = result)
    }

    @Test
    fun `loadEpisodes should update episodes state using mock`() = runTest {
        // Given
        val expectedEpisodes = listOf(defaultEpisode)
        val expectedPage = Page(items = expectedEpisodes, hasNextPage = true)

        every { mockUseCase.invoke(page = 1) } returns flowOf(Lce.Content(expectedPage))

        val viewModel = EpisodeViewModel(mockUseCase)

        // When
        viewModel.loadEpisodes()
        testScheduler.advanceUntilIdle() // Allow the coroutine to complete

        // Then
        val result = viewModel.episodes.first()

        assertEquals(expectedEpisodes, actual = result)
        verify(exactly = 1) { mockUseCase.invoke(page = 1) }
    }

    @Test
    fun `loadNextPageIfNeeded should accumulate pages and stop once hasNextPage is false`() = runTest {
        // Given
        val firstPageEpisode = listOf(defaultEpisode)
        val secondPageEpisode = listOf(createEpisode(id = 2, name = "Episode 1", episode = "S01E02"))
        every { mockUseCase.invoke(page = 1) } returns flowOf(
            Lce.Content(Page(items = firstPageEpisode, hasNextPage = true))
        )
        every { mockUseCase.invoke(page = 2) } returns flowOf(
            Lce.Content(Page(items = secondPageEpisode, hasNextPage = false))
        )

        val viewModel = EpisodeViewModel(getEpisodesUseCase = mockUseCase)

        // When
        viewModel.loadNextPageIfNeeded()
        viewModel.loadNextPageIfNeeded()
        viewModel.loadNextPageIfNeeded() // endReached: should be a no-op

        // Then
        assertEquals(firstPageEpisode + secondPageEpisode, viewModel.episodes.first())
        verify(exactly = 1) { mockUseCase.invoke(page = 1) }
        verify(exactly = 1) { mockUseCase.invoke(page = 2) }
        verify(exactly = 0) { mockUseCase.invoke(page = 3) }
    }

    @Test
    fun `loadNextPageIfNeeded should set errorMessage and leave episodes untouched when the use case emits an error`() = runTest {
        // Given
        every { mockUseCase.invoke(page = 1) } returns flowOf(Lce.Error(RuntimeException("boom")))

        val viewModel = EpisodeViewModel(getEpisodesUseCase = mockUseCase)

        // When
        viewModel.loadNextPageIfNeeded()

        // Then
        assertEquals(emptyList(), viewModel.episodes.first())
        assertEquals("The next page of episodes could not be loaded.", viewModel.errorMessage.first())
        assertEquals(false, viewModel.isLoadingNextPage.first())
    }

    @Test
    fun `loadNextPageIfNeeded should be a no-op while a page is already being fetched`() = runTest {
        // Given
        val page = Page(items = listOf(defaultEpisode), hasNextPage = true)
        every { mockUseCase.invoke(page = 1) } returns flow {
            emit(Lce.Loading)
            delay(1_000.milliseconds)
            emit(Lce.Content(page))
        }

        val viewModel = EpisodeViewModel(getEpisodesUseCase = mockUseCase)

        // When
        val firstCall = launch { viewModel.loadNextPageIfNeeded() }
        val secondCall = launch { viewModel.loadNextPageIfNeeded() }
        testScheduler.advanceUntilIdle()
        firstCall.join()
        secondCall.join()

        // Then
        verify(exactly = 1) { mockUseCase.invoke(page = 1) }
        assertNull(viewModel.errorMessage.first())
    }

    @Test
    fun `loadNextPageIfNeeded should keep isLoadingNextPage true until the minimum loading duration elapses`() = runTest {
        // Given: a use case that resolves immediately, with no delay of its own
        every { mockUseCase.invoke(page = 1) } returns flowOf(
            Lce.Content(Page(items = listOf(defaultEpisode), hasNextPage = true))
        )

        val viewModel = EpisodeViewModel(getEpisodesUseCase = mockUseCase)

        // When
        val job = launch { viewModel.loadNextPageIfNeeded() }
        testScheduler.runCurrent()

        // Then: still loading well before the 400ms minimum has elapsed
        testScheduler.advanceTimeBy(200.milliseconds)
        testScheduler.runCurrent()
        assertEquals(true, viewModel.isLoadingNextPage.first())

        // And: no longer loading once the minimum has elapsed
        testScheduler.advanceTimeBy(250.milliseconds)
        testScheduler.runCurrent()
        job.join()

        assertEquals(false, viewModel.isLoadingNextPage.first())
        assertEquals(listOf(defaultEpisode), viewModel.episodes.first())
    }

    @Test
    fun `loadNextPageIfNeeded should propagate CancellationException without setting an error message`() = runTest {
        // Given: a use case that is still in flight when the caller cancels
        every { mockUseCase.invoke(page = 1) } returns flow {
            emit(Lce.Loading)
            delay(1_000.milliseconds)
            emit(Lce.Content(Page(items = listOf(defaultEpisode), hasNextPage = true)))
        }

        val viewModel = EpisodeViewModel(getEpisodesUseCase = mockUseCase)

        // When
        val job = launch { viewModel.loadNextPageIfNeeded() }
        testScheduler.runCurrent() // let it reach the in-flight use case call
        job.cancel()
        testScheduler.advanceUntilIdle() // let the cancellation propagate and finally{} run
        job.join()

        // Then
        assertTrue(job.isCancelled)
        assertNull(viewModel.errorMessage.first())
        assertEquals(emptyList(), viewModel.episodes.first())
        assertEquals(false, viewModel.isLoadingNextPage.first())
    }
}

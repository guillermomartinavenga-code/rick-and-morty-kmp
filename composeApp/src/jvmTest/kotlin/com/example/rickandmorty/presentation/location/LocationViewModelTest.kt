package com.example.rickandmorty.presentation.location

import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.use_case.api.GetLocationsUseCase
import com.example.rickandmorty.testutil.createLocation
import com.example.rickandmorty.testutil.defaultLocation
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
 * Unit tests for the [LocationViewModel].
 */
@ExperimentalCoroutinesApi
class LocationViewModelTest {

    @MockK
    lateinit var mockUseCase: GetLocationsUseCase

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
    fun `loadLocations should update locations state using fake`() = runTest {
        // Given
        val expectedLocations = listOf(defaultLocation)
        val expectedPage = Page(items = expectedLocations, hasNextPage = true)
        val fakeUseCase = object : GetLocationsUseCase {
            override fun invoke(page: Int): Flow<Lce<Page<Location>>> {
                return flowOf(Lce.Content(expectedPage))
            }
        }

        val viewModel = LocationViewModel(getLocationsUseCase = fakeUseCase)

        // When
        viewModel.loadLocations()
        testScheduler.advanceUntilIdle() // Allow the coroutine to complete

        // Then
        val result = viewModel.locations.first()

        assertEquals(expectedLocations, result)
    }

    @Test
    fun `loadLocations should update locations state using mock`() = runTest {
        // Given
        val expectedLocations = listOf(defaultLocation)
        val expectedPage = Page(items = expectedLocations, hasNextPage = true)

        every { mockUseCase.invoke(page = 1) } returns flowOf(Lce.Content(expectedPage))

        val viewModel = LocationViewModel(getLocationsUseCase = mockUseCase)

        // When
        viewModel.loadLocations()
        testScheduler.advanceUntilIdle() // Allow the coroutine to complete

        // Then
        val result = viewModel.locations.first()

        assertEquals(expectedLocations, result)
        verify(exactly = 1) { mockUseCase.invoke(page = 1) }
    }

    @Test
    fun `loadNextPageIfNeeded should accumulate pages and stop once hasNextPage is false`() = runTest {
        // Given
        val firstPageLocation = listOf(defaultLocation)
        val secondPageLocation = listOf(createLocation(id = 2, name = "Location 2"))
        every { mockUseCase.invoke(page = 1) } returns flowOf(
            Lce.Content(Page(items = firstPageLocation, hasNextPage = true))
        )
        every { mockUseCase.invoke(page = 2) } returns flowOf(
            Lce.Content(Page(items = secondPageLocation, hasNextPage = false))
        )

        val viewModel = LocationViewModel(getLocationsUseCase = mockUseCase)

        // When
        viewModel.loadNextPageIfNeeded()
        viewModel.loadNextPageIfNeeded()
        viewModel.loadNextPageIfNeeded() // endReached: should be a no-op

        // Then
        assertEquals(firstPageLocation + secondPageLocation, viewModel.locations.first())
        verify(exactly = 1) { mockUseCase.invoke(page = 1) }
        verify(exactly = 1) { mockUseCase.invoke(page = 2) }
        verify(exactly = 0) { mockUseCase.invoke(page = 3) }
    }

    @Test
    fun `loadNextPageIfNeeded should set errorMessage and leave locations untouched when the use case emits an error`() = runTest {
        // Given
        every { mockUseCase.invoke(page = 1) } returns flowOf(Lce.Error(RuntimeException("boom")))

        val viewModel = LocationViewModel(getLocationsUseCase = mockUseCase)

        // When
        viewModel.loadNextPageIfNeeded()

        // Then
        assertEquals(emptyList(), viewModel.locations.first())
        assertEquals("The next page of locations could not be loaded.", viewModel.errorMessage.first())
        assertEquals(false, viewModel.isLoadingNextPage.first())
    }

    @Test
    fun `loadNextPageIfNeeded should be a no-op while a page is already being fetched`() = runTest {
        // Given
        val page = Page(items = listOf(defaultLocation), hasNextPage = true)
        every { mockUseCase.invoke(page = 1) } returns flow {
            emit(Lce.Loading)
            delay(1_000.milliseconds)
            emit(Lce.Content(page))
        }

        val viewModel = LocationViewModel(getLocationsUseCase = mockUseCase)

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
            Lce.Content(Page(items = listOf(defaultLocation), hasNextPage = true))
        )

        val viewModel = LocationViewModel(getLocationsUseCase = mockUseCase)

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
        assertEquals(listOf(defaultLocation), viewModel.locations.first())
    }

    @Test
    fun `loadNextPageIfNeeded should propagate CancellationException without setting an error message`() = runTest {
        // Given: a use case that is still in flight when the caller cancels
        every { mockUseCase.invoke(page = 1) } returns flow {
            emit(Lce.Loading)
            delay(1_000.milliseconds)
            emit(Lce.Content(Page(items = listOf(defaultLocation), hasNextPage = true)))
        }

        val viewModel = LocationViewModel(getLocationsUseCase = mockUseCase)

        // When
        val job = launch { viewModel.loadNextPageIfNeeded() }
        testScheduler.runCurrent() // let it reach the in-flight use case call
        job.cancel()
        testScheduler.advanceUntilIdle() // let the cancellation propagate and finally{} run
        job.join()

        // Then
        assertTrue(job.isCancelled)
        assertNull(viewModel.errorMessage.first())
        assertEquals(emptyList(), viewModel.locations.first())
        assertEquals(false, viewModel.isLoadingNextPage.first())
    }
}

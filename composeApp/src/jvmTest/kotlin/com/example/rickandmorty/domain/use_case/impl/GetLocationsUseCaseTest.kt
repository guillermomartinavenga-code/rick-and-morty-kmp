package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.LocationRepository
import com.example.rickandmorty.testutil.createLocation
import com.example.rickandmorty.testutil.defaultLocation
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for the [com.example.rickandmorty.domain.use_case.api.GetLocationsUseCase].
 */
class GetLocationsUseCaseTest {

    @MockK
    lateinit var mockRepository: LocationRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke should emit Loading then Content with the repository page using fake`() = runTest {
        // Given
        val locations = listOf(
            defaultLocation,
            createLocation(id = 2, name = "Citadel of Ricks", type = "Space station", dimension = "Unknown")
        )
        val expectedPage = Page(items = locations, hasNextPage = true)

        val fakeRepository = object : LocationRepository {
            override suspend fun getLocations(page: Int): Page<Location> = expectedPage
        }
        val useCase = GetLocationsUseCaseImpl(fakeRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(listOf(Lce.Loading, Lce.Content(expectedPage)), emissions)
    }

    @Test
    fun `invoke should emit Loading then Content with the repository page using mock`() = runTest {
        // Given
        val locations = listOf(
            defaultLocation,
            createLocation(id = 2, name = "Citadel of Ricks", type = "Space station", dimension = "Unknown")
        )
        val expectedPage = Page(items = locations, hasNextPage = true)

        coEvery { mockRepository.getLocations(1) }.returns(expectedPage)

        val useCase = GetLocationsUseCaseImpl(mockRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(listOf(Lce.Loading, Lce.Content(expectedPage)), emissions)
        coVerify(exactly = 1) { mockRepository.getLocations(1) }
    }

    @Test
    fun `invoke should emit Loading then Error when the repository throws`() = runTest {
        // Given
        val exception = RuntimeException("boom")
        coEvery { mockRepository.getLocations(1) } throws exception

        val useCase = GetLocationsUseCaseImpl(mockRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(Lce.Loading, emissions.first())
        val error = assertIs<Lce.Error>(emissions[1])
        assertEquals(exception, error.throwable)
        coVerify(exactly = 1) { mockRepository.getLocations(1) }
    }
}

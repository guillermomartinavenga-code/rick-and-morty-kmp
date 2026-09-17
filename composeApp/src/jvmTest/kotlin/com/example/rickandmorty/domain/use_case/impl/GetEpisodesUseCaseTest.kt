package com.example.rickandmorty.domain.use_case.impl

import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Lce
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.domain.repository.EpisodeRepository
import com.example.rickandmorty.testutil.createEpisode
import com.example.rickandmorty.testutil.defaultEpisode
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
 * Unit tests for the [com.example.rickandmorty.domain.use_case.api.GetEpisodesUseCase].
 */
class GetEpisodesUseCaseTest {

    @MockK
    lateinit var mockRepository: EpisodeRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke should emit Loading then Content with the repository page using fake`() = runTest {
        // Given
        val episodes = listOf(
            defaultEpisode,
            createEpisode(id = 2, name = "Lawnmower Dog", airDate = "December 9, 2013", episode = "S01E02")
        )
        val expectedPage = Page(items = episodes, hasNextPage = true)

        val fakeRepository = object : EpisodeRepository {
            override suspend fun getEpisodes(page: Int): Page<Episode> = expectedPage
        }
        val useCase = GetEpisodesUseCaseImpl(fakeRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(listOf(Lce.Loading, Lce.Content(expectedPage)), emissions)
    }

    @Test
    fun `invoke should emit Loading then Content with the repository page using mock`() = runTest {
        // Given
        val episodes = listOf(
            defaultEpisode,
            createEpisode(id = 2, name = "Lawnmower Dog", airDate = "December 9, 2013", episode = "S01E02")
        )
        val expectedPage = Page(items = episodes, hasNextPage = true)

        coEvery { mockRepository.getEpisodes(1) }.returns(expectedPage)

        val useCase = GetEpisodesUseCaseImpl(mockRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(listOf(Lce.Loading, Lce.Content(expectedPage)), emissions)
        coVerify(exactly = 1) { mockRepository.getEpisodes(1) }
    }

    @Test
    fun `invoke should emit Loading then Error when the repository throws`() = runTest {
        // Given
        val exception = RuntimeException("boom")
        coEvery { mockRepository.getEpisodes(1) } throws exception

        val useCase = GetEpisodesUseCaseImpl(mockRepository)

        // When
        val emissions = useCase(1).toList()

        // Then
        assertEquals(Lce.Loading, emissions.first())
        val error = assertIs<Lce.Error>(emissions[1])
        assertEquals(exception, error.throwable)
        coVerify(exactly = 1) { mockRepository.getEpisodes(1) }
    }
}

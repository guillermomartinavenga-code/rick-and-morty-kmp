package com.example.rickandmorty.domain.use_case

import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.repository.EpisodeRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the [GetEpisodesUseCase].
 */
class GetEpisodesUseCaseTest {

    @Test
    fun `invoke should return episodes from repository`() = runBlocking {
        // Given
        val episodes = listOf(
            Episode(1, "Pilot", "December 2, 2013", "S01E01"),
            Episode(2, "Lawnmower Dog", "December 9, 2013", "S01E02")
        )
        val mockRepository = object : EpisodeRepository {
            override suspend fun getEpisodes(): List<Episode> = episodes
        }
        val useCase = GetEpisodesUseCase(mockRepository)

        // When
        val result = useCase()

        // Then
        assertEquals(episodes, result)
    }
}

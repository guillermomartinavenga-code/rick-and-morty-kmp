package com.example.rickandmorty.data.remote

import arrow.core.Either
import com.example.rickandmorty.data.remote.kmpgen.api.CharacterApi
import com.example.rickandmorty.data.remote.kmpgen.api.EpisodeApi
import com.example.rickandmorty.data.remote.kmpgen.api.LocationApi
import com.example.rickandmorty.data.remote.kmpgen.models.Character
import com.example.rickandmorty.data.remote.kmpgen.models.Character200Response
import com.example.rickandmorty.data.remote.kmpgen.models.Episode
import com.example.rickandmorty.data.remote.kmpgen.models.Episode200Response
import com.example.rickandmorty.data.remote.kmpgen.models.Info
import com.example.rickandmorty.data.remote.kmpgen.models.Location
import com.example.rickandmorty.data.remote.kmpgen.models.Location200Response
import com.example.rickandmorty.domain.model.Page
import com.kroegerama.openapi.kmp.gen.companion.HttpCallResponse
import com.kroegerama.openapi.kmp.gen.companion.UnexpectedCallException
import io.ktor.client.statement.HttpResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ApiServiceImplTest {
    private val serviceImpl = ApiServiceImpl()

    @Nested
    inner class `get characters` {

        @BeforeTest
        fun setup() {
            mockkObject(CharacterApi)
        }

        @AfterTest
        fun tearDown() {
            unmockkObject(CharacterApi)
        }

        @Test
        fun `returns mapped page with hasNextPage true when the API reports a next link`() = runTest {
            // Given
            val apiCharacter = Character(id = 1, name = "Rick Sanchez")
            val info = Info(
                count = 826,
                pages = 42,
                next = "https://rickandmortyapi.com/api/character?page=2",
                prev = null
            )
            val apiResponse = Character200Response(info = info, results = listOf(apiCharacter))
            coEvery { CharacterApi.getAllCharacters(page = 1) } returns
                Either.Right(HttpCallResponse(
                    data = apiResponse,
                    raw = mockk<HttpResponse>(relaxed = true))
                )

            // When
            val result = serviceImpl.getCharacters(page = 1)

            // Then
            assertEquals(Page(
                items = listOf(apiCharacter.toDomain()),
                hasNextPage = true
            ), result)
            coVerify(exactly = 1) { CharacterApi.getAllCharacters(page = 1) }
        }

        @Test
        fun `returns hasNextPage false when the API reports no next link`() = runTest {
            // Given
            val info = Info(count = 1, pages = 1, next = null, prev = null)
            val apiResponse = Character200Response(info = info, results = emptyList())
            coEvery { CharacterApi.getAllCharacters(page = 1) } returns
                Either.Right(HttpCallResponse(
                    data = apiResponse,
                    raw = mockk<HttpResponse>(relaxed = true))
                )

            // When
            val result = serviceImpl.getCharacters(page = 1)

            // Then
            assertEquals(Page(items = emptyList(), hasNextPage = false), result)
        }

        @Test
        fun `rethrows the CallException carried by a Left result`() = runTest {
            // Given
            val error = UnexpectedCallException(message = "boom", cause = null)
            coEvery { CharacterApi.getAllCharacters(page = 1) } returns Either.Left(error)

            // When / Then
            val thrown = assertFailsWith<UnexpectedCallException> {
                serviceImpl.getCharacters(page = 1)
            }
            assertSame(error, thrown)
        }
    }

    @Nested
    inner class `get locations` {
        @BeforeTest
        fun setup() {
            mockkObject(LocationApi)
        }

        @AfterTest
        fun tearDown() {
            unmockkObject(LocationApi)
        }

        @Test
        fun `returns mapped page with hasNextPage true when the API reports a next link`() = runTest {
            // Given
            val apiLocation = Location(id = 1, name = "somewhere")
            val info = Info(
                count = 826,
                pages = 42,
                next = "https://rickandmortyapi.com/api/location?page=2",
                prev = null
            )
            val apiResponse = Location200Response(info = info, results = listOf(apiLocation))
            coEvery { LocationApi.getAllLocations(page = 1) } returns
                    Either.Right(HttpCallResponse(
                        data = apiResponse,
                        raw = mockk<HttpResponse>(relaxed = true))
                    )

            // When
            val result = serviceImpl.getLocations(page = 1)

            // Then
            assertEquals(Page(
                items = listOf(apiLocation.toDomain()),
                hasNextPage = true
            ), result)
            coVerify(exactly = 1) { LocationApi.getAllLocations(page = 1) }
        }

        @Test
        fun `returns hasNextPage false when the API reports no next link`() = runTest {
            // Given
            val info = Info(count = 1, pages = 1, next = null, prev = null)
            val apiResponse = Location200Response(info = info, results = emptyList())
            coEvery { LocationApi.getAllLocations(page = 1) } returns
                    Either.Right(HttpCallResponse(
                        data = apiResponse,
                        raw = mockk<HttpResponse>(relaxed = true))
                    )

            // When
            val result = serviceImpl.getLocations(page = 1)

            // Then
            assertEquals(Page(items = emptyList(), hasNextPage = false), result)
        }

        @Test
        fun `rethrows the CallException carried by a Left result`() = runTest {
            // Given
            val error = UnexpectedCallException(message = "boom", cause = null)
            coEvery { LocationApi.getAllLocations(page = 1) } returns Either.Left(error)

            // When / Then
            val thrown = assertFailsWith<UnexpectedCallException> {
                serviceImpl.getLocations(page = 1)
            }
            assertSame(error, thrown)
        }
    }

    @Nested
    inner class `get episodes` {
        @BeforeTest
        fun setup() {
            mockkObject(EpisodeApi)
        }

        @AfterTest
        fun tearDown() {
            unmockkObject(EpisodeApi)
        }

        @Test
        fun `returns mapped page with hasNextPage true when the API reports a next link`() = runTest {
            // Given
            val apiEpisode = Episode(id = 1, name = "S01E001")
            val info = Info(
                count = 826,
                pages = 42,
                next = "https://rickandmortyapi.com/api/episode?page=2",
                prev = null
            )
            val apiResponse = Episode200Response(info = info, results = listOf(apiEpisode))
            coEvery { EpisodeApi.getAllEpisodes(page = 1) } returns
                    Either.Right(HttpCallResponse(
                        data = apiResponse,
                        raw = mockk<HttpResponse>(relaxed = true))
                    )

            // When
            val result = serviceImpl.getEpisodes(page = 1)

            // Then
            assertEquals(Page(
                items = listOf(apiEpisode.toDomain()),
                hasNextPage = true
            ), result)
            coVerify(exactly = 1) { EpisodeApi.getAllEpisodes(page = 1) }
        }

        @Test
        fun `returns hasNextPage false when the API reports no next link`() = runTest {
            // Given
            val info = Info(count = 1, pages = 1, next = null, prev = null)
            val apiResponse = Episode200Response(info = info, results = emptyList())
            coEvery { EpisodeApi.getAllEpisodes(page = 1) } returns
                    Either.Right(HttpCallResponse(
                        data = apiResponse,
                        raw = mockk<HttpResponse>(relaxed = true))
                    )

            // When
            val result = serviceImpl.getEpisodes(page = 1)

            // Then
            assertEquals(Page(items = emptyList(), hasNextPage = false), result)
        }

        @Test
        fun `rethrows the CallException carried by a Left result`() = runTest {
            // Given
            val error = UnexpectedCallException(message = "boom", cause = null)
            coEvery { EpisodeApi.getAllEpisodes(page = 1) } returns Either.Left(error)

            // When / Then
            val thrown = assertFailsWith<UnexpectedCallException> {
                serviceImpl.getEpisodes(page = 1)
            }
            assertSame(error, thrown)
        }
    }
}

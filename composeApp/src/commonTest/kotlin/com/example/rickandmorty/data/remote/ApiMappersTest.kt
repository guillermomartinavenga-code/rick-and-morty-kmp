package com.example.rickandmorty.data.remote

import com.example.rickandmorty.data.remote.kmpgen.models.Info
import com.example.rickandmorty.data.remote.kmpgen.models.LocationRef
import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location
import com.example.rickandmorty.domain.model.Page
import org.junit.jupiter.api.Nested
import com.example.rickandmorty.data.remote.kmpgen.models.Character as CharacterDto
import com.example.rickandmorty.data.remote.kmpgen.models.Character200Response
import com.example.rickandmorty.data.remote.kmpgen.models.Episode as EpisodeDto
import com.example.rickandmorty.data.remote.kmpgen.models.Episode200Response
import com.example.rickandmorty.data.remote.kmpgen.models.Location as LocationDto
import com.example.rickandmorty.data.remote.kmpgen.models.Location200Response
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class ApiMappersTest {

    @Nested
    inner class `character dto` {
        private fun createCharacterDto(
            id: Long? = 1L,
            name: String? = "Rick Sanchez",
            status: CharacterDto.Status? = CharacterDto.Status.ALIVE,
            species: String? = "human",
            type: String? = "type",
            gender: CharacterDto.Gender? = CharacterDto.Gender.MALE,
            origin: LocationRef? = LocationRef(
                name = "Earth",
                url = "https://rickandmortyapi.com/api/location/1"
            ),
            location: LocationRef? = LocationRef(
                name = "Earth",
                url = "https://rickandmortyapi.com/api/location/1"
            ),
            image: String? = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
        ) = CharacterDto(
            id = id,
            name = name,
            status = status,
            species = species,
            type = type,
            gender = gender,
            origin = origin,
            location = location,
            image = image
        )

        @Test
        fun `toDomain should map every field to the domain Character when all properties are present`() {
            // Arrange
            val characterDto = createCharacterDto()

            val characterExpected = Character(
                id = 1,
                name = "Rick Sanchez",
                species = "human",
                gender = "Male",
                origin = "Earth",
                location = "Earth",
                image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
            )

            // Assert
            assertEquals(characterExpected, characterDto.toDomain())
        }

        @Test
        fun `toDomain applies defaults when optional fields are null`() {
            // Arrange
            val characterDto = createCharacterDto(
                id = null,
                name = null,
                species = null,
                type = null,
                status = null,
                gender = null,
                origin = null,
                location = null,
                image = null
            )

            val characterExpected = Character(
                id = 0,
                name = "",
                species = "",
                gender = "",
                origin = "",
                location = "",
                image = ""
            )

            // Assert
            assertEquals(characterExpected, characterDto.toDomain())
        }
    }

    @Nested
    inner class `location dto` {
        private fun createLocationDto(
            id: Long? = 1L,
            name: String? = "Earth",
            type: String? = "Planet",
            dimension: String? = "Dimension C-137"
        ) = LocationDto(id = id, name = name, type = type, dimension = dimension)

        @Test
        fun `toDomain should map every field to the domain Location when all properties are present`() {
            // Arrange
            val locationDto = createLocationDto()

            val locationExpected = Location(
                id = 1,
                name = "Earth",
                type = "Planet",
                dimension = "Dimension C-137"
            )

            // Assert
            assertEquals(locationExpected, locationDto.toDomain())
        }

        @Test
        fun `toDomain applies defaults when optional fields are null`() {
            // Arrange
            val locationDto = createLocationDto(id = null, name = null, type = null, dimension = null)

            val locationExpected = Location(id = 0, name = "", type = "", dimension = "")

            // Assert
            assertEquals(locationExpected, locationDto.toDomain())
        }
    }

    @Nested
    inner class `episode dto` {
        private fun createEpisodeDto(
            id: Long? = 1L,
            name: String? = "Pilot",
            airDate: String? = "December 2, 2013",
            episode: String? = "S01E01"
        ) = EpisodeDto(id = id, name = name, airDate = airDate, episode = episode)

        @Test
        fun `toDomain should map every field to the domain Episode when all properties are present`() {
            // Arrange
            val episodeDto = createEpisodeDto()

            val episodeExpected = Episode(
                id = 1,
                name = "Pilot",
                airDate = "December 2, 2013",
                episode = "S01E01"
            )

            // Assert
            assertEquals(episodeExpected, episodeDto.toDomain())
        }

        @Test
        fun `toDomain applies defaults when optional fields are null`() {
            // Arrange
            val episodeDto = createEpisodeDto(id = null, name = null, airDate = null, episode = null)

            val episodeExpected = Episode(id = 0, name = "", airDate = "", episode = "")

            // Assert
            assertEquals(episodeExpected, episodeDto.toDomain())
        }
    }

    @Nested
    inner class `character 200 response` {
        private fun createInfo(next: String? = "https://rickandmortyapi.com/api/character?page=2") =
            Info(count = 1, pages = 1, next = next, prev = null)

        @Test
        fun `toDomainPage maps results and marks hasNextPage true when info has a next link`() {
            // Arrange
            val apiCharacter = CharacterDto(id = 1, name = "Rick Sanchez")
            val response = Character200Response(info = createInfo(), results = listOf(apiCharacter))

            val pageExpected = Page(items = listOf(apiCharacter.toDomain()), hasNextPage = true)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }

        @Test
        fun `toDomainPage returns empty items and hasNextPage false when info and results are null`() {
            // Arrange
            val response = Character200Response(info = null, results = null)

            val pageExpected = Page<Character>(items = emptyList(), hasNextPage = false)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }

        @Test
        fun `toDomainPage maps results and marks hasNextPage false when info is present but next is null`() {
            // Arrange
            val apiCharacter = CharacterDto(id = 1, name = "Rick Sanchez")
            val response = Character200Response(info = createInfo(next = null), results = listOf(apiCharacter))

            val pageExpected = Page(items = listOf(apiCharacter.toDomain()), hasNextPage = false)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }
    }

    /**
     * Educational counterpart of [`character 200 response`][ApiMappersTest].
     *
     * The three `@Test` methods above each hardcode their own [Character200Response] and
     * expected [Page], even though they all exercise the very same production code
     * ([Character200Response.toDomainPage]) and only differ in the *data* they feed it. A
     * [ParameterizedTest] collapses that duplication into a single test method plus a table of
     * input/output rows, which is the idiomatic JUnit5 way to test one behavior against many
     * cases without copy-pasting the test body.
     *
     * [TestInstance.Lifecycle.PER_CLASS] is required here because [generateResponses] is a
     * regular (non-static) member function. By default JUnit5 needs `@MethodSource` methods to
     * be static, since it normally creates a fresh test-class instance per test method and has
     * no instance to call the source method on ahead of time; `PER_CLASS` tells it to reuse a
     * single instance for the whole class instead, which JUnit5 can call the source method on
     * before any test method runs.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class `parametrized test for character 200 response` {
        private fun createInfo(next: String? = "https://rickandmortyapi.com/api/character?page=2") =
            Info(count = 1, pages = 1, next = next, prev = null)

        private val apiCharacter = CharacterDto(id = 1, name = "Rick Sanchez")

        /**
         * The data table consumed by [`toDomainPage maps response to the expected page`]. Each
         * [Arguments.of] entry is one test case: a human-readable [String] description (shown in
         * the test report via `@ParameterizedTest(name = "...")`), the [Character200Response]
         * input, and the [Page] it should map to.
         *
         * The last row (`next` set to an empty, non-null string) is a deliberately chosen edge
         * case: it documents, via a passing test, that [Character200Response.toDomainPage]
         * currently treats *any* non-null `next` — including `""` — as "there is a next page",
         * since the production code only checks `info?.next != null` rather than also checking
         * for blankness.
         */
        fun generateResponses(): List<Arguments> = listOf(
            Arguments.of(
                "info has a next link",
                Character200Response(info = createInfo(), results = listOf(apiCharacter)),
                Page(items = listOf(apiCharacter.toDomain()), hasNextPage = true)
            ),
            Arguments.of(
                "info and results are null",
                Character200Response(info = null, results = null),
                Page<Character>(items = emptyList(), hasNextPage = false)
            ),
            Arguments.of(
                "info is present but next is null",
                Character200Response(info = createInfo(next = null), results = listOf(apiCharacter)),
                Page(items = listOf(apiCharacter.toDomain()), hasNextPage = false)
            ),
            Arguments.of(
                "next is an empty non-null string",
                Character200Response(info = createInfo(next = ""), results = listOf(apiCharacter)),
                Page(items = listOf(apiCharacter.toDomain()), hasNextPage = true)
            )
        )

        /**
         * Runs once per row returned by [generateResponses], with [description], [response] and
         * [expectedPage] bound to that row's three [Arguments.of] values in order. JUnit5 reports
         * each run as its own test result (named via `@ParameterizedTest(name = "toDomainPage
         * when {0}")`), so a failing case points straight at the offending row instead of forcing
         * a scan through one large method.
         */
        @ParameterizedTest(name = "toDomainPage when {0}")
        @MethodSource("generateResponses")
        fun `toDomainPage maps response to the expected page`(
            description: String,
            response: Character200Response,
            expectedPage: Page<Character>
        ) {
            // Assert
            assertEquals(expectedPage, response.toDomainPage())
        }
    }

    @Nested
    inner class `location 200 response` {
        private fun createInfo(next: String? = "https://rickandmortyapi.com/api/location?page=2") =
            Info(count = 1, pages = 1, next = next, prev = null)

        @Test
        fun `toDomainPage maps results and marks hasNextPage true when info has a next link`() {
            // Arrange
            val apiLocation = LocationDto(id = 1, name = "Earth")
            val response = Location200Response(info = createInfo(), results = listOf(apiLocation))

            val pageExpected = Page(items = listOf(apiLocation.toDomain()), hasNextPage = true)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }

        @Test
        fun `toDomainPage returns empty items and hasNextPage false when info and results are null`() {
            // Arrange
            val response = Location200Response(info = null, results = null)

            val pageExpected = Page<Location>(items = emptyList(), hasNextPage = false)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }

        @Test
        fun `toDomainPage maps results and marks hasNextPage false when info is present but next is null`() {
            // Arrange
            val apiLocation = LocationDto(id = 1, name = "Earth")
            val response = Location200Response(info = createInfo(next = null), results = listOf(apiLocation))

            val pageExpected = Page(items = listOf(apiLocation.toDomain()), hasNextPage = false)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }
    }

    /**
     * Educational counterpart of [`location 200 response`][ApiMappersTest], mirroring
     * [`parametrized test for character 200 response`][ApiMappersTest] but exercising
     * [Location200Response.toDomainPage] instead. See that class's KDoc for why the table-driven
     * approach removes the duplication in the three `@Test` methods above, and why
     * [TestInstance.Lifecycle.PER_CLASS] is required.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class `parametrized test for location 200 response` {
        private fun createInfo(next: String? = "https://rickandmortyapi.com/api/location?page=2") =
            Info(count = 1, pages = 1, next = next, prev = null)

        private val apiLocation = LocationDto(id = 1, name = "Earth")

        /**
         * The data table consumed by [`toDomainPage maps response to the expected page`]. As in
         * the character variant, the last row (`next` set to an empty, non-null string) documents
         * that [Location200Response.toDomainPage] treats any non-null `next` — including `""` —
         * as "there is a next page".
         */
        fun generateResponses(): List<Arguments> = listOf(
            Arguments.of(
                "info has a next link",
                Location200Response(info = createInfo(), results = listOf(apiLocation)),
                Page(items = listOf(apiLocation.toDomain()), hasNextPage = true)
            ),
            Arguments.of(
                "info and results are null",
                Location200Response(info = null, results = null),
                Page<Location>(items = emptyList(), hasNextPage = false)
            ),
            Arguments.of(
                "info is present but next is null",
                Location200Response(info = createInfo(next = null), results = listOf(apiLocation)),
                Page(items = listOf(apiLocation.toDomain()), hasNextPage = false)
            ),
            Arguments.of(
                "next is an empty non-null string",
                Location200Response(info = createInfo(next = ""), results = listOf(apiLocation)),
                Page(items = listOf(apiLocation.toDomain()), hasNextPage = true)
            )
        )

        /**
         * Runs once per row returned by [generateResponses], with [description], [response] and
         * [expectedPage] bound to that row's three [Arguments.of] values in order.
         */
        @ParameterizedTest(name = "toDomainPage when {0}")
        @MethodSource("generateResponses")
        fun `toDomainPage maps response to the expected page`(
            description: String,
            response: Location200Response,
            expectedPage: Page<Location>
        ) {
            // Assert
            assertEquals(expectedPage, response.toDomainPage())
        }
    }

    @Nested
    inner class `episode 200 response` {
        private fun createInfo(next: String? = "https://rickandmortyapi.com/api/episode?page=2") =
            Info(count = 1, pages = 1, next = next, prev = null)

        @Test
        fun `toDomainPage maps results and marks hasNextPage true when info has a next link`() {
            // Arrange
            val apiEpisode = EpisodeDto(id = 1, name = "Pilot")
            val response = Episode200Response(info = createInfo(), results = listOf(apiEpisode))

            val pageExpected = Page(items = listOf(apiEpisode.toDomain()), hasNextPage = true)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }

        @Test
        fun `toDomainPage returns empty items and hasNextPage false when info and results are null`() {
            // Arrange
            val response = Episode200Response(info = null, results = null)

            val pageExpected = Page<Episode>(items = emptyList(), hasNextPage = false)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }

        @Test
        fun `toDomainPage maps results and marks hasNextPage false when info is present but next is null`() {
            // Arrange
            val apiEpisode = EpisodeDto(id = 1, name = "Pilot")
            val response = Episode200Response(info = createInfo(next = null), results = listOf(apiEpisode))

            val pageExpected = Page(items = listOf(apiEpisode.toDomain()), hasNextPage = false)

            // Assert
            assertEquals(pageExpected, response.toDomainPage())
        }
    }

    /**
     * Educational counterpart of [`episode 200 response`][ApiMappersTest], mirroring
     * [`parametrized test for character 200 response`][ApiMappersTest] but exercising
     * [Episode200Response.toDomainPage] instead. See that class's KDoc for why the table-driven
     * approach removes the duplication in the three `@Test` methods above, and why
     * [TestInstance.Lifecycle.PER_CLASS] is required.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class `parametrized test for episode 200 response` {
        private fun createInfo(next: String? = "https://rickandmortyapi.com/api/episode?page=2") =
            Info(count = 1, pages = 1, next = next, prev = null)

        private val apiEpisode = EpisodeDto(id = 1, name = "Pilot")

        /**
         * The data table consumed by [`toDomainPage maps response to the expected page`]. As in
         * the character variant, the last row (`next` set to an empty, non-null string) documents
         * that [Episode200Response.toDomainPage] treats any non-null `next` — including `""` —
         * as "there is a next page".
         */
        fun generateResponses(): List<Arguments> = listOf(
            Arguments.of(
                "info has a next link",
                Episode200Response(info = createInfo(), results = listOf(apiEpisode)),
                Page(items = listOf(apiEpisode.toDomain()), hasNextPage = true)
            ),
            Arguments.of(
                "info and results are null",
                Episode200Response(info = null, results = null),
                Page<Episode>(items = emptyList(), hasNextPage = false)
            ),
            Arguments.of(
                "info is present but next is null",
                Episode200Response(info = createInfo(next = null), results = listOf(apiEpisode)),
                Page(items = listOf(apiEpisode.toDomain()), hasNextPage = false)
            ),
            Arguments.of(
                "next is an empty non-null string",
                Episode200Response(info = createInfo(next = ""), results = listOf(apiEpisode)),
                Page(items = listOf(apiEpisode.toDomain()), hasNextPage = true)
            )
        )

        /**
         * Runs once per row returned by [generateResponses], with [description], [response] and
         * [expectedPage] bound to that row's three [Arguments.of] values in order.
         */
        @ParameterizedTest(name = "toDomainPage when {0}")
        @MethodSource("generateResponses")
        fun `toDomainPage maps response to the expected page`(
            description: String,
            response: Episode200Response,
            expectedPage: Page<Episode>
        ) {
            // Assert
            assertEquals(expectedPage, response.toDomainPage())
        }
    }
}
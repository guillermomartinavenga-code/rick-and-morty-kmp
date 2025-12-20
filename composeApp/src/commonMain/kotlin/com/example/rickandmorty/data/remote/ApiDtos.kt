package com.example.rickandmorty.data.remote

import kotlinx.serialization.Serializable

// region Character DTOs

/**
 * Data transfer object for the character API response.
 * @property results The list of character DTOs.
 */
@Serializable
data class CharacterResponse(
    val results: List<CharacterDto>
)

/**
 * Data transfer object for a single character from the API.
 */
@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val species: String,
    val gender: String,
    val origin: CharacterLocationDto,
    val location: CharacterLocationDto,
    val image: String
)

/**
 * Represents the nested location object inside a character response.
 * It contains the name and URL of the location.
 */
@Serializable
data class CharacterLocationDto(
    val name: String,
    val url: String
)

// endregion

// region Location DTOs

/**
 * Data transfer object for the location API response.
 * @property results The list of location DTOs.
 */
@Serializable
data class LocationResponse(
    val results: List<LocationDto>
)

/**
 * Represents the full location object returned by the /location endpoint.
 */
@Serializable
data class LocationDto(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val url: String
)

// endregion

// region Episode DTOs

/**
 * Data transfer object for the episode API response.
 * @property results The list of episode DTOs.
 */
@Serializable
data class EpisodeResponse(
    val results: List<EpisodeDto>
)

/**
 * Data transfer object for a single episode from the API.
 * @property air_date The air date of the episode.
 */
@Serializable
data class EpisodeDto(
    val id: Int,
    val name: String,
    val air_date: String,
    val episode: String
)

// endregion

package com.example.rickandmorty.data.remote

import kotlinx.serialization.Serializable

/**
 * Data transfer object for the character response from the API.
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
    val origin: OriginDto,
    val location: LocationDto,
    val image: String
)

/**
 * Data transfer object for origin information.
 */
@Serializable
data class OriginDto(
    val name: String
)

/**
 * Data transfer object for location information.
 */
@Serializable
data class LocationDto(
    val name: String
)

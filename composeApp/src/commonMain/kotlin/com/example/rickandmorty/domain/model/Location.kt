package com.example.rickandmorty.domain.model

/**
 * Represents a location in the Rick and Morty universe.
 *
 * @property id The unique identifier for the location.
 * @property name The name of the location.
 * @property type The type of the location (e.g., 'Planet').
 * @property dimension The dimension in which the location is located.
 */
data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String
)

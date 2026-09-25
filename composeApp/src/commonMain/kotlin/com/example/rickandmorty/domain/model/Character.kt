package com.example.rickandmorty.domain.model

/**
 * Represents a character in the Rick and Morty universe.
 *
 * @property id The unique identifier for the character.
 * @property name The name of the character.
 * @property species The species of the character.
 * @property gender The gender of the character.
 * @property origin The origin location of the character.
 * @property location The last known location of the character.
 * @property image The URL for the character's image.
 */
data class Character(
    val id: Int,
    val name: String,
    val species: String,
    val gender: String,
    val origin: String,
    val location: String,
    val image: String
)

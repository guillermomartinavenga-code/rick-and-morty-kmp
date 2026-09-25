package com.example.rickandmorty.domain.model

/**
 * Represents an episode in the Rick and Morty universe.
 *
 * @property id The unique identifier for the episode.
 * @property name The name of the episode.
 * @property airDate The air date of the episode.
 * @property episode The code of the episode (e.g., "S01E01").
 */
data class Episode(
    val id: Int,
    val name: String,
    val airDate: String,
    val episode: String
)

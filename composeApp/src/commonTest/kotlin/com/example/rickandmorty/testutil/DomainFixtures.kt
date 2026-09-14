package com.example.rickandmorty.testutil

import com.example.rickandmorty.domain.model.Character
import com.example.rickandmorty.domain.model.Episode
import com.example.rickandmorty.domain.model.Location

/**
 * Shared test data builders for the domain models, reused across repository,
 * use case and view model tests so the "canonical" fixture stays in one place.
 */
fun createCharacter(
    id: Int = 1,
    name: String = "Rick Sanchez",
    species: String = "Human",
    gender: String = "Male",
    origin: String = "Earth (C-137)",
    location: String = "Citadel of Ricks",
    image: String = "image_url"
) = Character(id, name, species, gender, origin, location, image)

/**
 * Canonical [Character] instance. Prefer `.copy(...)` over [createCharacter] when a test
 * only needs to override one or two fields of an otherwise-default character.
 */
val defaultCharacter = createCharacter()

fun createLocation(
    id: Int = 1,
    name: String = "Earth (C-137)",
    type: String = "Planet",
    dimension: String = "Dimension C-137"
) = Location(id, name, type, dimension)

/**
 * Canonical [Location] instance. Prefer `.copy(...)` over [createLocation] when a test
 * only needs to override one or two fields of an otherwise-default location.
 */
val defaultLocation = createLocation()

fun createEpisode(
    id: Int = 1,
    name: String = "Pilot",
    airDate: String = "December 2, 2013",
    episode: String = "S01E01"
) = Episode(id, name, airDate, episode)

/**
 * Canonical [Episode] instance. Prefer `.copy(...)` over [createEpisode] when a test
 * only needs to override one or two fields of an otherwise-default episode.
 */
val defaultEpisode = createEpisode()

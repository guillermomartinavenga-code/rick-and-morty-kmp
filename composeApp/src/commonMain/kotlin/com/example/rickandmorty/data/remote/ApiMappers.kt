package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character as DomainCharacter
import com.example.rickandmorty.domain.model.Episode as DomainEpisode
import com.example.rickandmorty.domain.model.Location as DomainLocation
import com.example.rickandmorty.models.components.schemas.Character.Character as CharacterApiModel
import com.example.rickandmorty.models.components.schemas.Episode.Episode as EpisodeApiModel
import com.example.rickandmorty.models.components.schemas.Location.Location as LocationApiModel

fun CharacterApiModel.toDomain() = DomainCharacter(
    id = id,
    name = name.orEmpty(),
    species = species.orEmpty(),
    gender = gender?.toString().orEmpty(),      // el generador tipó gender como enum, no String
    origin = origin?.name.orEmpty(),      // LocationRef, no CharacterLocationDto
    location = location?.name.orEmpty(),
    image = image.orEmpty()
)

fun LocationApiModel.toDomain() = DomainLocation(
    id = id,
    name = name.orEmpty(),
    type = type.orEmpty(),
    dimension = dimension.orEmpty()
)

fun EpisodeApiModel.toDomain() = DomainEpisode(
    id = id,
    name = name.orEmpty(),
    airDate = airDate.orEmpty(),  // ⚠️ confirmar: el generador probablemente convirtió air_date → airDate
    episode = episode.orEmpty()
)
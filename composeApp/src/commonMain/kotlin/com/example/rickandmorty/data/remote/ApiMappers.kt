package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character as DomainCharacter
import com.example.rickandmorty.domain.model.Episode as DomainEpisode
import com.example.rickandmorty.domain.model.Location as DomainLocation
import rickandmorty.models.components.schemas.Character.Character as CharacterApiModel
import rickandmorty.models.components.schemas.Episode.Episode as EpisodeApiModel
import rickandmorty.models.components.schemas.Location.Location as LocationApiModel

fun CharacterApiModel.toDomain() = DomainCharacter(
    id = id,
    name = name.orEmpty(),
    species = species.orEmpty(),
    gender = gender?.toString().orEmpty(),
    origin = origin?.name.orEmpty(),
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
    airDate = airDate.orEmpty(),
    episode = episode.orEmpty()
)
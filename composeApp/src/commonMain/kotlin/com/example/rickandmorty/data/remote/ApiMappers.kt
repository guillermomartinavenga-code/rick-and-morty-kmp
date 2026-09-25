package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character as DomainCharacter
import com.example.rickandmorty.domain.model.Episode as DomainEpisode
import com.example.rickandmorty.domain.model.Location as DomainLocation
import com.example.rickandmorty.domain.model.Page
import com.example.rickandmorty.data.remote.kmpgen.models.Character as CharacterApiModel
import com.example.rickandmorty.data.remote.kmpgen.models.Episode as EpisodeApiModel
import com.example.rickandmorty.data.remote.kmpgen.models.Location as LocationApiModel
import com.example.rickandmorty.data.remote.kmpgen.models.Character200Response
import com.example.rickandmorty.data.remote.kmpgen.models.Location200Response
import com.example.rickandmorty.data.remote.kmpgen.models.Episode200Response

fun CharacterApiModel.toDomain() = DomainCharacter(
    id = id?.toInt() ?: 0,
    name = name.orEmpty(),
    species = species.orEmpty(),
    gender = toLowerCaseAndCapital(gender?.name),
    origin = origin?.name.orEmpty(),
    location = location?.name.orEmpty(),
    image = image.orEmpty()
)

fun LocationApiModel.toDomain() = DomainLocation(
    id = id?.toInt() ?: 0,
    name = name.orEmpty(),
    type = type.orEmpty(),
    dimension = dimension.orEmpty()
)

fun EpisodeApiModel.toDomain() = DomainEpisode(
    id = id?.toInt() ?: 0,
    name = name.orEmpty(),
    airDate = airDate.orEmpty(),
    episode = episode.orEmpty()
)

fun Character200Response.toDomainPage() = Page(
    items = results.orEmpty().map { it.toDomain() },
    hasNextPage = info?.next != null
)

fun Location200Response.toDomainPage() = Page(
    items = results.orEmpty().map { it.toDomain() },
    hasNextPage = info?.next != null
)

fun Episode200Response.toDomainPage() = Page(
    items = results.orEmpty().map { it.toDomain() },
    hasNextPage = info?.next != null
)

fun toLowerCaseAndCapital(text: String?): String {
    return text?.lowercase()?.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    } ?: ""
}

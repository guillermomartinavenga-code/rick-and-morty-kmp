package com.example.rickandmorty.data.remote

import com.example.rickandmorty.domain.model.Character as DomainCharacter
import com.example.rickandmorty.domain.model.Episode as DomainEpisode
import com.example.rickandmorty.domain.model.Location as DomainLocation
import com.example.rickandmorty.data.remote.kmpgen.models.Character as CharacterApiModel
import com.example.rickandmorty.data.remote.kmpgen.models.Episode as EpisodeApiModel
import com.example.rickandmorty.data.remote.kmpgen.models.Location as LocationApiModel
import java.util.Locale.getDefault
import kotlin.text.lowercase

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

fun toLowerCaseAndCapital(text: String?): String {
    return text?.lowercase(getDefault())?.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
    } ?: ""
}
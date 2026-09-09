package com.example.rickandmorty.domain.model

/**
 * Represents the lifecycle of an asynchronous operation exposed as a [kotlinx.coroutines.flow.Flow]:
 * in progress, succeeded with [Content.data], or failed with [Error.throwable].
 */
sealed class Lce<out T> {
    data object Loading : Lce<Nothing>()
    data class Content<T>(val data: T) : Lce<T>()
    data class Error(val throwable: Throwable) : Lce<Nothing>()
}

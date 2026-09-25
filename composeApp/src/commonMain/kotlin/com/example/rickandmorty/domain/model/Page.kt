package com.example.rickandmorty.domain.model

/**
 * A single page of results from a paginated list endpoint.
 *
 * @property items The items returned for this page.
 * @property hasNextPage Whether a subsequent page is available.
 */
data class Page<T>(
    val items: List<T>,
    val hasNextPage: Boolean
)

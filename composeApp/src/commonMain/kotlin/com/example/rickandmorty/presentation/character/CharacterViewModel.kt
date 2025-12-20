package com.example.rickandmorty.presentation.character

import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the state and business logic for the character list screen.
 * This class is a plain Kotlin class and does not extend Android's ViewModel,
 * making it fully compatible with Kotlin Multiplatform.
 */
class CharacterViewModel {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    /**
     * A [StateFlow] that emits the current list of characters.
     */
    val characters = _characters.asStateFlow()

    /**
     * Fetches the list of characters from the use case and updates the state.
     * This is a suspend function and should be called from a coroutine.
     */
    suspend fun loadCharacters() {
        _characters.value = Module.getCharactersUseCase()
    }
}

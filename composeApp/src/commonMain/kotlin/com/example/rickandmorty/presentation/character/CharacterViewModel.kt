package com.example.rickandmorty.presentation.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.di.Module
import com.example.rickandmorty.domain.model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the character list screen.
 * This class is responsible for fetching characters and managing the UI state.
 */
class CharacterViewModel : ViewModel() {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters = _characters.asStateFlow()

    init {
        viewModelScope.launch {
            _characters.value = Module.getCharactersUseCase()
        }
    }
}

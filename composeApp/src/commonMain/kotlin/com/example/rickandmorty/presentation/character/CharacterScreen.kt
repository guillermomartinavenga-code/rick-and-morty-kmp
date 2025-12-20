package com.example.rickandmorty.presentation.character

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Composable function for displaying the character list screen.
 *
 * @param viewModel The ViewModel for the character screen.
 */
@Composable
fun CharacterScreen(viewModel: CharacterViewModel = viewModel()) {
    val characters by viewModel.characters.collectAsState()

    LazyColumn {
        items(characters) { character ->
            CharacterRow(character = character)
        }
    }
}

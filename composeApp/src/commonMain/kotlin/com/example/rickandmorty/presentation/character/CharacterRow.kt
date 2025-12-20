package com.example.rickandmorty.presentation.character

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rickandmorty.domain.model.Character

/**
 * Composable function for displaying a single character row.
 *
 * @param character The character to display.
 */
@Composable
fun CharacterRow(character: Character) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Here you can add an Image composable to load the character image
        // For simplicity, we are only showing text
        Column {
            Text(text = "Name: ${character.name}")
            Text(text = "Species: ${character.species}")
            Text(text = "Gender: ${character.gender}")
        }
    }
}

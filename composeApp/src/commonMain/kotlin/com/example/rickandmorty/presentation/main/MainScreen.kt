package com.example.rickandmorty.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rickandmorty.presentation.theme.SpotifyGreen

/**
 * The main screen of the application, which provides navigation to different sections.
 *
 * @param onCharactersClick A callback to navigate to the characters screen.
 * @param onLocationsClick A callback to navigate to the locations screen.
 * @param onEpisodesClick A callback to navigate to the episodes screen.
 */
@Composable
fun MainScreen(
    onCharactersClick: () -> Unit,
    onLocationsClick: () -> Unit,
    onEpisodesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onCharactersClick,
            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
        ) {
            Text("Characters", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLocationsClick,
            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
        ) {
            Text("Locations", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEpisodesClick,
            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
        ) {
            Text("Episodes", color = Color.White)
        }
    }
}

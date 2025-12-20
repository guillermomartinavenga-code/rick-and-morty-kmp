package com.example.rickandmorty.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.rickandmorty.presentation.character.CharacterScreen
import com.example.rickandmorty.presentation.episode.EpisodeScreen
import com.example.rickandmorty.presentation.location.LocationScreen
import com.example.rickandmorty.presentation.main.MainScreen

/**
 * Manages the navigation flow of the application.
 */
@Composable
fun NavigationHost() {
    val currentScreen = remember { mutableStateOf<Screen>(Screen.Main) }

    when (currentScreen.value) {
        is Screen.Main -> MainScreen(
            onCharactersClick = { currentScreen.value = Screen.Characters },
            onLocationsClick = { currentScreen.value = Screen.Locations },
            onEpisodesClick = { currentScreen.value = Screen.Episodes }
        )
        is Screen.Characters -> CharacterScreen(onBack = { currentScreen.value = Screen.Main })
        is Screen.Locations -> LocationScreen(onBack = { currentScreen.value = Screen.Main })
        is Screen.Episodes -> EpisodeScreen(onBack = { currentScreen.value = Screen.Main })
    }
}

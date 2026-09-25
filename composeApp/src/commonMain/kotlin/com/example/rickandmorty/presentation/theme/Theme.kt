package com.example.rickandmorty.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SpotifyGreen,
    secondary = SpotifyBlack,
    background = SpotifyBlack,
    surface = SpotifyBlack,
    onPrimary = SpotifyWhite,
    onSecondary = SpotifyWhite,
    onBackground = SpotifyWhite,
    onSurface = SpotifyWhite
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

package com.example.rickandmorty.presentation.navigation

/**
 * Defines the navigation routes for the application.
 */
sealed class Screen(val route: String) {
    /**
     * Represents the main screen with navigation options.
     */
    data object Main : Screen("main")

    /**
     * Represents the screen that displays a list of characters.
     */
    data object Characters : Screen("characters")

    /**
     * Represents the screen that displays a list of locations.
     */
    data object Locations : Screen("locations")

    /**
     * Represents the screen that displays a list of episodes.
     */
    data object Episodes : Screen("episodes")
}

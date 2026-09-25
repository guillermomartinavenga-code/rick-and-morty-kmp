package com.example.rickandmorty

import androidx.compose.runtime.Composable
import com.example.rickandmorty.presentation.navigation.NavigationHost
import com.example.rickandmorty.presentation.theme.AppTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        NavigationHost()
    }
}

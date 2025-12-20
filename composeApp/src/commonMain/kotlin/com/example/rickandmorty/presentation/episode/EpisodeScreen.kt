package com.example.rickandmorty.presentation.episode

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable function for displaying the episode list screen.
 *
 * @param onBack A callback to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeScreen(onBack: () -> Unit) {
    val viewModel = remember { EpisodeViewModel() }
    val episodes by viewModel.episodes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEpisodes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Episodes List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 10.dp)
                .padding(vertical = 20.dp)
        ) {
            items(episodes) { episode ->
                EpisodeRow(episode = episode)
            }
        }
    }
}

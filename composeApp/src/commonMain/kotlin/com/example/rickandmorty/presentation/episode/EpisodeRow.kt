package com.example.rickandmorty.presentation.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rickandmorty.domain.model.Episode

/**
 * Composable function for displaying a single episode row.
 *
 * @param episode The episode to display.
 */
@Composable
fun EpisodeRow(episode: Episode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            Text(text = "Name: ${episode.name}")
            Text(text = "Air Date: ${episode.airDate}")
            Text(text = "Episode: ${episode.episode}")
        }
    }
}

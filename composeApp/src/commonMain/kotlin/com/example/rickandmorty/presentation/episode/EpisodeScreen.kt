package com.example.rickandmorty.presentation.episode

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    val isLoadingNextPage by viewModel.isLoadingNextPage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadEpisodes()
    }

    val isAtEnd by remember(episodes.size) {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            episodes.isNotEmpty() && lastVisibleItemIndex >= episodes.size - 1
        }
    }

    LaunchedEffect(isAtEnd) {
        if (isAtEnd) {
            viewModel.loadNextPageIfNeeded()
        }
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
            state = listState,
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 10.dp)
                .padding(vertical = 20.dp)
        ) {
            items(episodes) { episode ->
                EpisodeRow(episode = episode)
            }
            if (isLoadingNextPage) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }
        }
    }
}

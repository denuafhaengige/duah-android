package com.denuafhaengige.duahandroid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun App(viewModel: AppViewModel) {

    val playbackState by viewModel.playbackState.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Den Uafhængige") },
                backgroundColor = Color.Black,
                contentColor = Color.White,
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ActionButton(
                    playing = playbackState == AppViewModel.PlaybackState.PLAYING,
                    action = { viewModel.onPlaybackButtonTapped() },
                    disabled = playbackState == AppViewModel.PlaybackState.LOADING
                )
            }
        }
    )
}

@Composable
fun ActionButton(disabled: Boolean = false, playing: Boolean = false, action: () -> Unit) {

    val icon = if (playing) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled

    IconButton(
        onClick = {
            action()
        },
        modifier = Modifier
            .size(width = 150.dp, height = 150.dp),
        enabled = !disabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Play",
            Modifier
                .fillMaxSize()
        )
    }
}

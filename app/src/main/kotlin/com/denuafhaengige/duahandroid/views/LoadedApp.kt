package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.models.BroadcastFetcher
import com.google.accompanist.insets.navigationBarsPadding

@Composable
fun LoadedApp(viewModel: AppViewModel) {


    val liveChannel by viewModel.liveChannel.observeAsState()

    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier.navigationBarsPadding(),
            topBar = {
                DynamicLogoTopAppBar(
                    playerViewModel = viewModel.playerViewModel,
                    liveChannel = liveChannel,
                    navController = navController,
                )
            },
            content = { paddingValues ->
                Navigation(
                    navController = navController,
                    viewModel = viewModel,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                )
            },
            bottomBar = {
                AnimatedTinyPlayer(
                    model = TinyPlayerModel(playerViewModel = viewModel.playerViewModel)
                )
            },
        )
        liveChannel?.let { liveChannel ->
            DynamicLargePlayer(
                playerViewModel = viewModel.playerViewModel,
                liveChannel = liveChannel,
                broadcastFetcher = BroadcastFetcher(store = Application.contentProvider.contentStore)
            )
        }
    }
}
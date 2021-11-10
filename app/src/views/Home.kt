package com.denuafhaengige.duahandroid.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.models.BroadcastFetcher

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Home(viewModel: AppViewModel) {

    val featuredContent by viewModel.featuredContent.observeAsState()
    val latestBroadcasts by viewModel.latestBroadcasts.observeAsState()
    val scrollState = rememberScrollState()
    val liveChannel by viewModel.liveChannel.observeAsState()

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier.navigationBarsPadding(),
            topBar = {
                LogoTopAppBar(
                    model = LogoTopAppBarModel(
                        playerViewModel = viewModel.playerViewModel,
                        liveChannel = liveChannel
                    )
                )
             },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(paddingValues)
                ) {
                    featuredContent?.let {
                        FeaturedPager(
                            content = it,
                            playerViewModel = viewModel.playerViewModel,
                        )
                    }
                    latestBroadcasts?.let {
                        LatestBroadcasts(
                            playableBroadcasts = it,
                            playerViewModel = viewModel.playerViewModel
                        )
                    }
                }
            },
            bottomBar = {
                AnimatedTinyPlayer(
                    model = TinyPlayerModel(playerViewModel = viewModel.playerViewModel)
                )
            },
        )
        viewModel.contentService?.contentStore?.let { contentStore ->
            liveChannel?.let { liveChannel ->
                DynamicLargePlayer(
                    playerViewModel = viewModel.playerViewModel,
                    liveChannel = liveChannel,
                    broadcastFetcher = BroadcastFetcher(store = contentStore)
                )
            }
        }
    }
    
    
}

package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LivePlayableBroadcast
import com.denuafhaengige.duahandroid.util.capitalizeWords

@Composable
fun LatestBroadcasts(
    playableBroadcasts: List<LivePlayableBroadcast>,
    playerViewModel: PlayerViewModel,
    navController: NavController,
) {

    val lazyRowState = rememberLazyListState()

    ContentRow(
        title = casedStringResource(id = R.string.title_latest_broadcasts),
        titleFarRightItem = {
            NavigationButton(
                text = stringResource(id = R.string.all).uppercase(),
                action = { navController.navigate(NavigationRouteDest.BroadcastsList().destRoute) },
            )
        },
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .padding(vertical = ContentDimensions.contentRowVerticalPadding)
            .fillMaxWidth(),
    ) {
        LazyRow(
            state = lazyRowState,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            items(items = playableBroadcasts) { playableBroadcast ->
                DynamicLatestBroadcastsItem(playableBroadcast, playerViewModel, navController)
            }
        }
    }
}

@Composable
fun DynamicLatestBroadcastsItem(
    livePlayableBroadcast: LivePlayableBroadcast,
    playerViewModel: PlayerViewModel,
    navController: NavController,
) {

    val observedPlayableBroadcast by livePlayableBroadcast.livePlayableBroadcast.observeAsState()
    val playableBroadcast = observedPlayableBroadcast ?: return

    Column(
        modifier = Modifier
            .width(ContentDimensions.squareBannerSize)
            .height(250.dp)
            .clickable {
                navController.navigate(
                    route = NavigationRouteDest.Broadcast(playableBroadcast).destRoute
                )
            },
    ) {
        BroadcastVisual(
            broadcast = playableBroadcast.broadcast,
            style = BroadcastVisualStyle.SQUARE,
            modifier = Modifier
                .padding(bottom = 10.dp)
                .size(ContentDimensions.squareBannerSize),
        ) {
            DynamicBroadcastVisualPlayButton(
                playerViewModel = playerViewModel,
                broadcast = playableBroadcast.broadcast,
            )
        }
        MetaTitleTextForContent(
            content = playableBroadcast,
            modifier = Modifier.padding(bottom = 5.dp),
        )
        SmallTitleTextForContent(content = playableBroadcast)
    }
}

@Composable
fun LatestBroadcastsItem() {

}

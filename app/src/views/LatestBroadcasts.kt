package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.unit.dp
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.Broadcast
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LiveEntity
import com.denuafhaengige.duahandroid.util.LivePlayableBroadcast
import com.denuafhaengige.duahandroid.util.capitalizeWords

@Composable
fun LatestBroadcasts(
    playableBroadcasts: List<LivePlayableBroadcast>,
    playerViewModel: PlayerViewModel
) {

    val lazyRowState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
    ) {
        Text(
            text = stringResource(id = R.string.title_latest_broadcasts).replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.h1,
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 25.dp, bottom = 0.dp),
        )
        LazyRow(
            state = lazyRowState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 30.dp),
        ) {
            items(items = playableBroadcasts) { playableBroadcast ->
                LatestBroadcastsItem(playableBroadcast, playerViewModel)
            }
        }
    }
}

@Composable
fun LatestBroadcastsItem(
    livePlayableBroadcast: LivePlayableBroadcast,
    playerViewModel: PlayerViewModel
) {

    val playableBroadcast by livePlayableBroadcast.livePlayableBroadcast.observeAsState()

    val metaTitle =
        playableBroadcast?.broadcast?.metaTitle ?:
        stringResource(id = R.string.fallback_program_title).capitalizeWords()

    Column(
        modifier = Modifier
            .width(150.dp)
            .height(250.dp),
    ) {
        playableBroadcast?.let {
            BroadcastVisual(
                playableBroadcast = it,
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 10.dp),
                hostPhotoDiameter = 40.dp,
                style = BroadcastVisualStyle.SQUARE,
                playerViewModel,
            )
        }
        Text(
            text = metaTitle,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        playableBroadcast?.let {
            Text(text = it.broadcast.title, style = MaterialTheme.typography.subtitle2)
        }
    }
}
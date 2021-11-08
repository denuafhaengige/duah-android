package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LiveEntity

data class LogoTopAppBarModel(
    val playerViewModel: PlayerViewModel,
    val liveChannel: LiveEntity<ChannelWithCurrentBroadcast>?,
)

@Composable
fun LogoTopAppBar(model: LogoTopAppBarModel) {

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsHeight()
                .background(color = MaterialTheme.colors.surface),
        )
        Box(
            modifier = Modifier
                .shadow(2.dp, clip = false)
        ) {
            TopAppBar(
                backgroundColor = Color.White,
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Spacer(modifier = Modifier)
                    model.liveChannel?.let {
                        Box(modifier = Modifier.padding(end = 5.dp)) {
                            LogoTopAppBarLiveChannelButton(
                                playerViewModel = model.playerViewModel,
                                liveChannel = it,
                            )
                        }
                    }
                }
            }
            Image(
                painter = painterResource(id = R.drawable.round_icon),
                contentDescription = null, // decorative element
                modifier = Modifier
                    .scale(1.5F)
                    .align(alignment = Alignment.Center)
            )
        }
    }

}

@Composable
fun LogoTopAppBarLiveChannelButton(
    playerViewModel: PlayerViewModel,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>,
) {
    val channel by liveChannel.liveEntity.observeAsState()

    channel?.let {
        DynamicPlaybackButton(
            playerViewModel = playerViewModel,
            playable = Playable.Channel(channel = it),
            style = PlaybackButtonStyle.LIVE,
            modifier = Modifier.size(width = 80.dp, height = 40.dp),
        )
    }

}


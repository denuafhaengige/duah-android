package com.denuafhaengige.duahandroid.views

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayableFlow
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LivePlayable
import com.denuafhaengige.duahandroid.util.LivePlayableBroadcast

enum class BroadcastVisualStyle {
    WIDE,
    SQUARE,
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BroadcastVisual(
    playableBroadcast: Playable.Broadcast,
    modifier: Modifier,
    hostPhotoDiameter: Dp,
    style: BroadcastVisualStyle,
    playerViewModel: PlayerViewModel,
) {

    val imageUri = when (style) { // TODO: Use default image
        BroadcastVisualStyle.WIDE -> playableBroadcast.broadcast.wideImageUri
        BroadcastVisualStyle.SQUARE -> playableBroadcast.broadcast.squareImageUri
    }
    val date = playableBroadcast.broadcast.broadcast.broadcasted
    val hosts = playableBroadcast.broadcast.employees

    Box(modifier) {
        imageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
            )
        }
        Column() {
            date?.let { DayMonthLabel(date = date) }
            val hostPhotoUrlStrings = hosts.mapNotNull { it.photoFile?.url }
            if (hostPhotoUrlStrings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(vertical = hostPhotoDiameter.div(4), horizontal = hostPhotoDiameter.div(6)),
                    horizontalArrangement = Arrangement.spacedBy(hostPhotoDiameter.div(8)),
                ) {
                    for (hostPhotoUrlString in hostPhotoUrlStrings) {
                        CircleImage(
                            uri = Uri.parse(hostPhotoUrlString),
                            diameter = hostPhotoDiameter,
                            borderColor = Color.White)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = hostPhotoDiameter.div(4), horizontal = hostPhotoDiameter.div(6)),
            contentAlignment = Alignment.BottomEnd,
        ) {
            DynamicPlaybackButton(
                playerViewModel = playerViewModel,
                playable = playableBroadcast,
                style = PlaybackButtonStyle.CIRCLE,
                modifier = Modifier
                    .size(hostPhotoDiameter)
            )
        }
    }
}
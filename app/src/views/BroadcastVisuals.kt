package com.denuafhaengige.duahandroid.views

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel

enum class BroadcastVisualStyle {
    WIDE,
    SQUARE,
}

@Composable
fun DynamicBroadcastVisualPlayButton(
    playerViewModel: PlayerViewModel,
    broadcast: BroadcastWithProgramAndEmployees,
) {
    BroadcastVisualPlayButton { verticalPadding, horizontalPadding, diameter ->
        DynamicPlaybackButton(
            playerViewModel = playerViewModel,
            playable = Playable.Broadcast(broadcast),
            style = PlaybackButtonStyle.NEW_CIRCLE,
            modifier = Modifier
                .padding(
                    vertical = verticalPadding,
                    horizontal = horizontalPadding,
                )
                .size(diameter),
        )
    }
}

@Composable
fun BroadcastVisualPlayButton(
    playButton: @Composable (
        verticalPadding: Dp,
        horizontalPadding: Dp,
        diameter: Dp,
    ) -> Unit,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .fillMaxSize()
    ) {
        playButton(
            verticalPadding = maxHeight.div(12),
            horizontalPadding = maxHeight.div(14),
            diameter = maxHeight.div(4),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BroadcastVisual(
    broadcast: BroadcastWithProgramAndEmployees,
    style: BroadcastVisualStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val imageUri = when (style) { // TODO: Use default image
        BroadcastVisualStyle.WIDE -> broadcast.wideImageUri
        BroadcastVisualStyle.SQUARE -> broadcast.squareImageUri
    }
    val date = broadcast.broadcast.broadcasted
    val hosts = broadcast.employees

    BoxWithConstraints(modifier) {

        val verticalPadding = maxHeight.div(16)
        val horizontalPadding = maxHeight.div(16)
        val hostPhotoDiameter = maxHeight.div(4)
        val hostPhotosSpacing = maxHeight.div(24)

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
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            date?.let { DayMonthLabel(date = date) }
            val hostPhotoUrlStrings = hosts.mapNotNull { it.photoFile?.url }
            if (hostPhotoUrlStrings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(vertical = verticalPadding, horizontal = horizontalPadding)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space = hostPhotosSpacing),
                ) {
                    for (hostPhotoUrlString in hostPhotoUrlStrings) {
                        CircleImage(
                            uri = Uri.parse(hostPhotoUrlString),
                            borderColor = Color.White,
                            modifier = Modifier
                                .size(hostPhotoDiameter),
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun BroadcastVisualWidePreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ContentDimensions.wideBannerHeight),
    ) {
        BroadcastVisual(
            broadcast = BroadcastWithProgramAndEmployees.example,
            style = BroadcastVisualStyle.WIDE,
        ) {
            BroadcastVisualPlayButton { verticalPadding, horizontalPadding, diameter ->
                PlaybackButton(
                    style = PlaybackButtonStyle.NEW_CIRCLE,
                    variant = PlaybackButtonVariant.PLAY,
                    modifier = Modifier
                        .padding(vertical = verticalPadding, horizontal = horizontalPadding)
                        .size(diameter),
                    action = {},
                )
            }
        }
    }
}

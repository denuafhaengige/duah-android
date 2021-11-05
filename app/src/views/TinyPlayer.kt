package com.denuafhaengige.duahandroid.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.theming.RedColor
import com.denuafhaengige.duahandroid.util.DurationFormatter
import com.denuafhaengige.duahandroid.util.LivePlayable

data class TinyPlayerModel(
    val playerViewModel: PlayerViewModel,
    val height: Dp = 70.dp,
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedTinyPlayer(model: TinyPlayerModel) {

    val livePlayable by model.playerViewModel.playable.observeAsState()
    val toggle = livePlayable != null

    AnimatedVisibility(
        visible = toggle,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth()
                .height(model.height),
            contentAlignment = Alignment.CenterStart,
        ) {
            livePlayable?.let {
                TinyPlayerContent(
                    height = model.height,
                    livePlayable = it,
                    playerViewModel = model.playerViewModel,
                )
            }
        }
    }
}

@Composable
fun TinyPlayerContent(height: Dp, livePlayable: LivePlayable, playerViewModel: PlayerViewModel) {

    val observedPlayable by livePlayable.livePlayable.observeAsState()
    val playable = observedPlayable ?: return

    val timeIndicatorWidth = 80.dp
    val textPadding = 4.dp

    Row(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PlaybackButton(
            style = PlaybackButtonStyle.PLAIN,
            playerViewModel = playerViewModel,
            playable = playable,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F),
        )
        TinyPlayerTimeIndicator(
            modifier = Modifier
                .fillMaxHeight()
                .width(timeIndicatorWidth),
            playable = playable,
            playerViewModel = playerViewModel,
        )
    }
    TinyPlayerDescriptors(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = height + textPadding,
                end = timeIndicatorWidth + textPadding,
                top = textPadding,
                bottom = textPadding,
            ),
        playable = playable,
    )
}

@Composable
private fun TinyPlayerDescriptors(modifier: Modifier, playable: Playable) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        Column {
            Text(
                text = playable.title,
                style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    fontSize = 12.sp,
                ),
            )
            playable.metaTitle?.let {
                Text(
                    text = it,
                    style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        fontSize = 10.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TinyPlayerTimeIndicator(modifier: Modifier, playable: Playable, playerViewModel: PlayerViewModel) {

    val duration by playerViewModel.duration.observeAsState()
    val position by playerViewModel.position.observeAsState()
    val hms = hmsString(duration, position)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            playable is Playable.Channel ->
                TinyPlayerLiveLabel()
            playable is Playable.Broadcast && hms != null ->
                TinyPlayerTimeLabel(text = hms)
            playable is Playable.Broadcast ->
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
        }
    }
}



@Composable
private fun TinyPlayerTimeLabel(text: String) {

    val timeIndicatorEndPadding = 20.dp

    Text(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            fontSize = 14.sp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = timeIndicatorEndPadding),
    )
}

@Composable
private fun TinyPlayerLiveLabel() {
    Row(
        modifier = Modifier
            .size(width = 60.dp, height = 25.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(30.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(8.dp)
                .background(Color.Black, shape = CircleShape),
        )
        Text(
            text = "LIVE",
            style = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
            ),
        )
    }
}

private fun hmsString(duration: Long?, position: Long?): String? {
    val nonNullDuration = duration ?: return null
    val nonNullPosition = position ?: return null
    val remainingSeconds = (nonNullDuration - nonNullPosition) / 1000
    return DurationFormatter.secondsToHMS(remainingSeconds.toInt())
}

@Preview
@Composable
private fun TinyPlayerLiveLabelPreview() {
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 80.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        TinyPlayerLiveLabel()
    }
}

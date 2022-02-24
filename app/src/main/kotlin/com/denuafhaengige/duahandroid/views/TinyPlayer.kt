package com.denuafhaengige.duahandroid.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.denuafhaengige.duahandroid.members.MembersViewModel
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.theming.RedColor
import com.denuafhaengige.duahandroid.theming.VeryDarkerGrey
import com.denuafhaengige.duahandroid.util.DurationFormatter
import com.denuafhaengige.duahandroid.util.LivePlayable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedTinyPlayer(
    membersViewModel: MembersViewModel,
    playerViewModel: PlayerViewModel,
    height: Dp = 70.dp,
) {

    val livePlayable by playerViewModel.playable.observeAsState()
    val toggle = livePlayable != null

    AnimatedVisibility(
        visible = toggle,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(
            modifier = Modifier
                .background(VeryDarkerGrey)
                .fillMaxWidth()
                .height(height)
                .clickable { playerViewModel.toggleLargePlayer.value = true },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawAppBarBorderBehind(AppBarBorderVariant.TOP)
            )
            livePlayable?.let {
                TinyPlayerContent(
                    height = height,
                    livePlayable = it,
                    membersViewModel = membersViewModel,
                    playerViewModel = playerViewModel,
                )
            }
        }
    }
}

@Composable
fun TinyPlayerContent(
    height: Dp,
    livePlayable: LivePlayable,
    membersViewModel: MembersViewModel,
    playerViewModel: PlayerViewModel,
) {

    val observedPlayable by livePlayable.livePlayable.observeAsState()
    val playable = observedPlayable ?: return

    val timeIndicatorWidth = 80.dp
    val textPadding = 4.dp

    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DynamicPlaybackButton(
            style = PlaybackButtonStyle.PLAIN,
            membersViewModel = membersViewModel,
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
                maxLines = 2,
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
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TinyPlayerTimeIndicator(modifier: Modifier, playable: Playable, playerViewModel: PlayerViewModel) {

    val duration by playerViewModel.duration.observeAsState()
    val position by playerViewModel.position.observeAsState()

    val durationPosition = duration?.let { nonNullDuration ->
        position?.let { nonNullPosition ->
            Pair(nonNullDuration, nonNullPosition)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            playable is Playable.Channel ->
                LiveLabel()
            playable is Playable.Broadcast && durationPosition != null ->
                TinyPlayerTimeLabel(
                    text = DurationFormatter.remainingDurationHMS(
                        duration = durationPosition.first,
                        position = durationPosition.second,
                    )
                )
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

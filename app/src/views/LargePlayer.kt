package com.denuafhaengige.duahandroid.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.Player
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.player.StreamType
import com.denuafhaengige.duahandroid.util.DurationFormatter
import com.denuafhaengige.duahandroid.util.Log
import com.google.accompanist.insets.statusBarsPadding

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DynamicLargePlayer(playerViewModel: PlayerViewModel) {

    val toggle by playerViewModel.toggleLargePlayer.observeAsState(false)
    val closeButtonAction = { playerViewModel.toggleLargePlayer.value = !toggle }
    val observedLivePlayable by playerViewModel.playable.observeAsState()
    val livePlayable = observedLivePlayable ?: return
    val observedPlayable by livePlayable.livePlayable.observeAsState()
    val playable = observedPlayable ?: return

    AnimatedVisibility(
        visible = toggle,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        LargePlayer(
            playable = playable,
            closeButtonAction = closeButtonAction,
            playbackControls = {
                DynamicLargePlayerPlaybackControls(playerViewModel, playable)
            },
            seekControl = {
                DynamicLargePlayerSeekControl(playerViewModel)
            },
        )
    }

}

@Composable
fun LargePlayer(
    playable: Playable,
    closeButtonAction: () -> Unit,
    playbackControls: @Composable ColumnScope.() -> Unit,
    seekControl: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = .9F))
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LargePlayerCloseButton(
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp),
            action = closeButtonAction,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LargePlayerPlayableVisual(playable = playable)
            Column {
                playbackControls()
                seekControl()
            }
        }
    }
}

@Composable
fun LargePlayerCloseButton(modifier: Modifier = Modifier, action: () -> Unit) {
    IconButton(
        onClick = action,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun LargePlayerPlayableVisual(playable: Playable, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        playable.squareImageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth(.7F)
                    .aspectRatio(1F)
                    .border(10.dp, Color.LightGray, RectangleShape),
            )
        }
        Text(
            text = playable.title,
            style = TextStyle(
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        )
        playable.metaTitle?.let {
            Text(
                text = it,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .padding(top = 5.dp),
            )
        }
    }
}

@Composable
fun DynamicLargePlayerPlaybackControls(playerViewModel: PlayerViewModel, playable: Playable) {
    LargePlayerPlaybackControls(
        jumpToStartButton = {},
        jumpBack15SecButton = {},
        playbackButton = {
            DynamicPlaybackButton(
                playerViewModel = playerViewModel,
                playable = playable,
                style = PlaybackButtonStyle.PLAIN,
                modifier = Modifier
                    .size(100.dp),
            )
        },
        jumpForward15SecButton = {},
        jumpToLiveButton = {},
        modifier = Modifier
            .fillMaxWidth(),
    )
}

@Composable
fun LargePlayerPlaybackControls(
    jumpToStartButton: @Composable RowScope.() -> Unit,
    jumpBack15SecButton: @Composable RowScope.() -> Unit,
    playbackButton: @Composable RowScope.() -> Unit,
    jumpForward15SecButton: @Composable RowScope.() -> Unit,
    jumpToLiveButton: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        jumpToStartButton()
        jumpBack15SecButton()
        playbackButton()
        jumpForward15SecButton()
        jumpToLiveButton()
    }
}

@Composable
fun DynamicLargePlayerSeekControl(playerViewModel: PlayerViewModel) {

    val observedStream by playerViewModel.stream.observeAsState()
    val stream = observedStream ?: return
    val observedPlayerState by playerViewModel.state.observeAsState()
    val duration by playerViewModel.duration.observeAsState()
    val position by playerViewModel.position.observeAsState()
    var seekTarget by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(observedPlayerState) {
        Log.debug("DynamicLargePlayerSeekControl | LaunchedEffect | observedPlayerState: $observedPlayerState")
        observedPlayerState?.let {
            if (it !is Player.State.Loading) {
                seekTarget = null
            }
        }
    }

    LargePlayerSeekControl(
        variant =
            if (stream.type == StreamType.LIVE_AAC) LargePlayerSeekControlVariant.LIVE
            else LargePlayerSeekControlVariant.VOD,
        position = seekTarget ?: position,
        duration = duration,
        onSeekTargetChanged = { seekTarget = it },
        onSeekDone = { seekTarget?.let { playerViewModel.player.seek(it) } }
    )
}

enum class LargePlayerSeekControlVariant {
    LIVE,
    VOD,
}

@Composable
fun LargePlayerSeekControl(
    variant: LargePlayerSeekControlVariant,
    duration: Long? = null,
    position: Long? = null,
    onSeekTargetChanged: (Long) -> Unit = {},
    onSeekDone: () -> Unit = {},
) {

    val textStyle = TextStyle(
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    )

    val sliderColors = SliderDefaults.colors(
        disabledActiveTrackColor = Color.DarkGray,
        disabledInactiveTrackColor = Color.DarkGray,
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        activeTickColor = Color.White,
        inactiveTrackColor = Color.DarkGray,
        inactiveTickColor = Color.DarkGray,
    )

    val sliderModifier = Modifier
        .fillMaxWidth()

    val disabledSlider = @Composable {
        Slider(
            value = .5F,
            onValueChange = {},
            modifier = sliderModifier,
            enabled = false,
            valueRange = 0F..1F,
            colors = sliderColors,
        )
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            when {
                variant == LargePlayerSeekControlVariant.LIVE -> {
                    Spacer(modifier = Modifier)
                    LiveLabel()
                }
                variant == LargePlayerSeekControlVariant.VOD && duration != null && position != null -> {
                    Text(
                        text = DurationFormatter.secondsToHMS((position/1000).toInt()),
                        style = textStyle,
                    )
                    Text(
                        text = DurationFormatter.remainingDurationHMS(duration, position),
                        style = textStyle,
                    )
                }
                variant == LargePlayerSeekControlVariant.VOD -> {
                    Spacer(modifier = Modifier)
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(20.dp),
                    )
                }
            }
        }
        when {
            variant == LargePlayerSeekControlVariant.LIVE ||
            variant == LargePlayerSeekControlVariant.VOD && (duration == null || position == null) -> {
                disabledSlider()
            }
            variant == LargePlayerSeekControlVariant.VOD && duration != null && position != null -> {
                Slider(
                    value = position.toFloat(),
                    onValueChange = { value ->
                        onSeekTargetChanged(value.toLong())
                    },
                    modifier = sliderModifier,
                    colors = sliderColors,
                    onValueChangeFinished = onSeekDone,
                    valueRange = 0F..duration.toFloat(),
                    steps = 1000,
                )
            }
        }
    }
}

@Preview
@Composable
private fun LiveLargePlayerPreview() {
    LargePlayer(
        closeButtonAction = ({}),
        playable = Playable.example,
        playbackControls = {
            LargePlayerPlaybackControls(
                jumpToStartButton = {},
                jumpBack15SecButton = {},
                playbackButton = {
                    PlaybackButton(
                        style = PlaybackButtonStyle.PLAIN,
                        variant = PlaybackButtonVariant.PLAY,
                        modifier = Modifier
                            .size(100.dp),
                        action = {}
                    )
                },
                jumpForward15SecButton = {},
                jumpToLiveButton = {},
                modifier = Modifier
                    .fillMaxWidth(),
            )
        },
        seekControl = {
            LargePlayerSeekControl(
                variant = LargePlayerSeekControlVariant.LIVE,
                duration = null,
                position = null
            )
        },
    )
}

@Preview
@Composable
private fun VodLargePlayerPreview() {
    LargePlayer(
        closeButtonAction = ({}),
        playable = Playable.example,
        playbackControls = {
            LargePlayerPlaybackControls(
                jumpToStartButton = {},
                jumpBack15SecButton = {},
                playbackButton = {
                     PlaybackButton(
                         style = PlaybackButtonStyle.PLAIN,
                         variant = PlaybackButtonVariant.PLAY,
                         modifier = Modifier
                             .size(100.dp),
                         action = {}
                     )
                },
                jumpForward15SecButton = {},
                jumpToLiveButton = {},
                modifier = Modifier
                    .fillMaxWidth(),
            )
        },
        seekControl = {
            LargePlayerSeekControl(
                variant = LargePlayerSeekControlVariant.VOD,
                duration = (((2*60*60)+(5*60))*1000).toLong(),
                position = ((20*60)*1000).toLong(),
            )
        },
    )
}

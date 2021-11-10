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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.BroadcastFetcher
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.Player
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.player.StreamType
import com.denuafhaengige.duahandroid.util.DurationFormatter
import com.denuafhaengige.duahandroid.util.LiveEntity
import com.denuafhaengige.duahandroid.util.Log
import com.google.accompanist.insets.statusBarsPadding

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DynamicLargePlayer(
    playerViewModel: PlayerViewModel,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>,
    broadcastFetcher: BroadcastFetcher,
) {

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
                DynamicLargePlayerPlaybackControls(
                    playerViewModel,
                    playable,
                    liveChannel,
                    broadcastFetcher,
                )
            },
            seekControl = {
                DynamicLargePlayerSeekControl(playerViewModel, liveChannel)
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
                Spacer(modifier = Modifier.height(20.dp))
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

    val imagePainter =
        if (playable.squareImageUri != null) rememberImagePainter(playable.squareImageUri)
        else painterResource(id = R.drawable.logo_white_on_black)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = imagePainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .fillMaxWidth(.7F)
                .aspectRatio(1F)
                .border(10.dp, Color.LightGray, RectangleShape),
        )
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
fun DynamicLargePlayerPlaybackControls(
    playerViewModel: PlayerViewModel,
    playable: Playable,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>,
    broadcastFetcher: BroadcastFetcher,
) {
    LargePlayerPlaybackControls(
        jumpToStartButton = {
            DynamicJumpButton(
                variant = JumpButtonVariant.BACK_START,
                playerViewModel = playerViewModel,
                liveChannel = liveChannel,
                broadcastFetcher = broadcastFetcher,
                modifier = Modifier
                    .size(50.dp),
            )
        },
        jumpBack15SecButton = {
            DynamicJumpButton(
                variant = JumpButtonVariant.BACK_15,
                playerViewModel = playerViewModel,
                liveChannel = liveChannel,
                broadcastFetcher = broadcastFetcher,
                modifier = Modifier
                    .size(50.dp),
            )
        },
        playbackButton = {
            DynamicPlaybackButton(
                playerViewModel = playerViewModel,
                playable = playable,
                style = PlaybackButtonStyle.NEW_CIRCLE,
                modifier = Modifier
                    .size(80.dp)
            )
        },
        jumpForward15SecButton = {
            DynamicJumpButton(
                variant = JumpButtonVariant.FORWARD_15,
                playerViewModel = playerViewModel,
                liveChannel = liveChannel,
                broadcastFetcher = broadcastFetcher,
                modifier = Modifier
                    .size(50.dp),
            )
        },
        jumpToLiveButton = {
            DynamicJumpButton(
                variant = JumpButtonVariant.FORWARD_LIVE,
                playerViewModel = playerViewModel,
                liveChannel = liveChannel,
                broadcastFetcher = broadcastFetcher,
                modifier = Modifier
                    .size(50.dp),
            )
        },
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
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        jumpToStartButton()
        jumpBack15SecButton()
        playbackButton()
        jumpForward15SecButton()
        jumpToLiveButton()
    }
}

@Composable
fun DynamicLargePlayerSeekControl(playerViewModel: PlayerViewModel, liveChannel: LiveEntity<ChannelWithCurrentBroadcast>) {

    val observedStream by playerViewModel.stream.observeAsState()
    val stream = observedStream ?: return
    val observedPlayerState by playerViewModel.state.observeAsState()
    val duration by playerViewModel.duration.observeAsState()
    val position by playerViewModel.position.observeAsState()
    val observedChannel by liveChannel.liveEntity.observeAsState()
    val channel = observedChannel ?: return
    var seekTarget by remember { mutableStateOf<Long?>(null) }
    val seekingBeyond2MinLeft = seekTarget?.let { nonNullSeekTarget -> duration?.let { nonNullDuration ->
        nonNullDuration - nonNullSeekTarget < (2 * 60 * 1000).toLong()
    }} == true

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
        overrideHideDurationPositionShowLiveLabel =
            stream.type == StreamType.HLS_EVENT && seekingBeyond2MinLeft,
        onSeekTargetChanged = { seekTarget = it },
        onSeekDone = { seekTarget?.let {
            if (stream.type == StreamType.HLS_EVENT && seekingBeyond2MinLeft) {
                playerViewModel.player.play(Playable.Channel(channel))
            } else {
                playerViewModel.player.seek(it)
            }
        }}
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
    overrideHideDurationPositionShowLiveLabel: Boolean = false,
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
                .fillMaxWidth()
                .height(35.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            when {
                variant == LargePlayerSeekControlVariant.LIVE || overrideHideDurationPositionShowLiveLabel -> {
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
                jumpToStartButton = {
                    JumpButton(
                        variant = JumpButtonVariant.BACK_START,
                        action = {},
                        modifier = Modifier.size(60.dp)
                    )
                },
                jumpBack15SecButton = {
                    JumpButton(
                        variant = JumpButtonVariant.BACK_15,
                        action = {},
                        modifier = Modifier.size(60.dp)
                    )
                },
                playbackButton = {
                    PlaybackButton(
                        style = PlaybackButtonStyle.NEW_CIRCLE,
                        variant = PlaybackButtonVariant.PLAY,
                        modifier = Modifier
                            .size(100.dp),
                        action = {}
                    )
                },
                jumpForward15SecButton = {
                    JumpButton(
                        variant = JumpButtonVariant.FORWARD_15,
                        action = {},
                        modifier = Modifier.size(60.dp)
                    )
                },
                jumpToLiveButton = {
                    JumpButton(
                        variant = JumpButtonVariant.FORWARD_LIVE,
                        action = {},
                        modifier = Modifier.size(60.dp)
                    )
                },
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
                jumpToStartButton = {
                    JumpButton(
                        variant = JumpButtonVariant.BACK_START,
                        action = {},
                        modifier = Modifier.size(50.dp),
                        enabled = false,
                    )
                },
                jumpBack15SecButton = {
                    JumpButton(
                        variant = JumpButtonVariant.BACK_15,
                        action = {},
                        modifier = Modifier.size(50.dp)
                    )
                },
                playbackButton = {
                    PlaybackButton(
                        style = PlaybackButtonStyle.NEW_CIRCLE,
                        variant = PlaybackButtonVariant.PLAY,
                        modifier = Modifier
                            .size(80.dp),
                        action = {}
                    )
                },
                jumpForward15SecButton = {
                    JumpButton(
                        variant = JumpButtonVariant.FORWARD_15,
                        action = {},
                        modifier = Modifier.size(50.dp)
                    )
                },
                jumpToLiveButton = {
                    JumpButton(
                        variant = JumpButtonVariant.FORWARD_LIVE,
                        action = {},
                        modifier = Modifier.size(50.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
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

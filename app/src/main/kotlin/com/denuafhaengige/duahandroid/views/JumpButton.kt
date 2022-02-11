package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.BroadcastFetcher
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.Player
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.DateUtil
import com.denuafhaengige.duahandroid.util.LiveEntity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

enum class JumpButtonVariant {
    BACK_START,
    BACK_15,
    FORWARD_15,
    FORWARD_LIVE,
}

@Composable
fun DynamicJumpButton(
    variant: JumpButtonVariant,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>,
    broadcastFetcher: BroadcastFetcher,
) {
    val observedLivePlayable by playerViewModel.playable.observeAsState()
    val livePlayable = observedLivePlayable ?: return
    val observedPlayable by livePlayable.livePlayable.observeAsState()
    val playable = observedPlayable ?: return
    val observedChannel by liveChannel.liveEntity.observeAsState()
    val channel = observedChannel ?: return
    val duration by playerViewModel.duration.observeAsState()
    val position by playerViewModel.position.observeAsState()
    val playerState by playerViewModel.state.observeAsState()
    var date2MinAgo by remember { mutableStateOf(DateUtil.nowMinusMinutes(2)) }

    LaunchedEffect(date2MinAgo) {
        delay(1000)
        date2MinAgo = DateUtil.nowMinusMinutes(2)
    }

    val enabled = when (variant) {
        JumpButtonVariant.FORWARD_LIVE ->
            playable is Playable.Broadcast &&
            playable.id == channel.currentBroadcast?.id &&
            playerState !is Player.State.Loading
        JumpButtonVariant.FORWARD_15 ->
            playable is Playable.Broadcast &&
            duration?.let { nonNullDuration ->
                position?.let { nonNullPosition ->
                    nonNullDuration - nonNullPosition > 15000
                }
            } == true &&
            playerState !is Player.State.Loading
        JumpButtonVariant.BACK_15 ->
            playable is Playable.Broadcast && position?.let { it > 15000 } == true &&
            playerState !is Player.State.Loading
        JumpButtonVariant.BACK_START ->
            (
                (
                    playable is Playable.Channel &&
                    playable.channel.currentBroadcast?.broadcasted?.let {
                        it < date2MinAgo
                    } == true
                ) ||
                playable is Playable.Broadcast
            ) &&
            playerState !is Player.State.Loading
    }

    val action: () -> Unit = when (variant) {
        JumpButtonVariant.FORWARD_LIVE ->
            ({ playerViewModel.player.play(Playable.Channel(channel)) })
        JumpButtonVariant.FORWARD_15 ->
            ({ position?.let { playerViewModel.player.seek(it + 15000) } })
        JumpButtonVariant.BACK_15 ->
            ({ position?.let { playerViewModel.player.seek(it - 15000) } })
        JumpButtonVariant.BACK_START ->
            ({
                when (playable) {
                    is Playable.Broadcast -> {
                        playerViewModel.player.seek(0)
                    }
                    is Playable.Channel -> {
                        playable.channel.currentBroadcast?.let {
                            MainScope().launch {
                                val broadcast = broadcastFetcher.get(it.id)
                                broadcast?.let {
                                    playerViewModel.player.play(Playable.Broadcast(it))
                                }
                            }
                        }
                    }
                }
            })
    }

    JumpButton(
        variant = variant,
        modifier = modifier,
        enabled = enabled,
        action = action,
    )
}

@Composable
fun JumpButton(
    variant: JumpButtonVariant,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    action: () -> Unit
) {
    val resource = when (variant) {
        JumpButtonVariant.BACK_15 -> painterResource(id = R.drawable.ic_back_15)
        JumpButtonVariant.BACK_START -> painterResource(id = R.drawable.ic_back_start)
        JumpButtonVariant.FORWARD_15 -> painterResource(id = R.drawable.ic_forward_15)
        JumpButtonVariant.FORWARD_LIVE -> painterResource(id = R.drawable.ic_forward_live)
    }

    val contentDescription = when (variant) {
        JumpButtonVariant.BACK_15 -> "Back 15"
        JumpButtonVariant.BACK_START -> "Back Start"
        JumpButtonVariant.FORWARD_15 -> "Forward 15"
        JumpButtonVariant.FORWARD_LIVE -> "Forward Live"
    }

    val tint =
        if (enabled) Color.White
        else Color.DarkGray

    IconButton(
        onClick = action,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            painter = resource,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize(),
            tint = tint,
        )
    }
}

@Preview
@Composable
private fun JumpButtonsPreview() {

    val action = ({})
    val modifier = Modifier.size(60.dp)

    Row(
        modifier = Modifier
            .width(300.dp)
            .height(100.dp)
            .background(Color.Black),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        JumpButton(
            variant = JumpButtonVariant.BACK_START,
            action = action,
            modifier = modifier,
            enabled = false,
        )
        JumpButton(
            variant = JumpButtonVariant.BACK_15,
            action = action,
            modifier = modifier,
        )
        JumpButton(
            variant = JumpButtonVariant.FORWARD_15,
            action = action,
            modifier = modifier,
        )
        JumpButton(
            variant = JumpButtonVariant.FORWARD_LIVE,
            action = action,
            modifier = modifier,
            enabled = false,
        )
    }
}
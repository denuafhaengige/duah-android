package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LiveEntity

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
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>?
) {
    val observedLivePlayable by playerViewModel.playable.observeAsState()
    val livePlayable = observedLivePlayable ?: return
    val observedPlayable by livePlayable.livePlayable.observeAsState()
    val playable = observedPlayable ?: return
    val nonNullLiveChannel = liveChannel ?: return
    val optionalChannel by nonNullLiveChannel.liveEntity.observeAsState()
    val channel = optionalChannel ?: return
    val duration by playerViewModel.duration.observeAsState()
    val position by playerViewModel.position.observeAsState()

    val enabled = when (variant) {
        JumpButtonVariant.FORWARD_LIVE ->
            playable is Playable.Broadcast && playable.id == channel.currentBroadcast?.id
        JumpButtonVariant.FORWARD_15 ->
            playable is Playable.Broadcast && duration?.let { nonNullDuration ->
                position?.let { nonNullPosition ->
                    nonNullDuration - nonNullPosition > 15000
                }
            } == true
        JumpButtonVariant.BACK_15 ->
            playable is Playable.Broadcast && position?.let { it > 15000 } == true
        JumpButtonVariant.BACK_START ->
            (playable is Playable.Channel && playable.channel.currentBroadcast != null) ||
            playable is Playable.Broadcast
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
                        val broadcast = playable.channel.currentBroadcast
//                        playerViewModel.player.play(Playable.Broadcast(broadcast))
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

    IconButton(
        onClick = action,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            painter = resource,
            contentDescription = "Back 15",
            modifier = Modifier
                .fillMaxSize(.7F),
            tint = Color.White,
        )
    }
}

package com.denuafhaengige.duahandroid.views

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.Employee
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.Player
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.theming.RedColor
import com.denuafhaengige.duahandroid.util.LivePlayable

enum class PlaybackButtonStyle {
    PLAIN,
    CIRCLE,
    NEW_CIRCLE,
    LIVE,
}

enum class PlaybackButtonVariant {
    PLAY,
    PAUSE,
    LOADING,
}

@Composable
fun DynamicPlaybackButton(
    playerViewModel: PlayerViewModel,
    playable: Playable,
    style: PlaybackButtonStyle,
    modifier: Modifier = Modifier,
) {
    val observedPlayerState by playerViewModel.state.observeAsState()
    val observedLivePlayerPlayable by playerViewModel.playable.observeAsState()

    val playerState = observedPlayerState ?: return

    val playableIsPlayerPlayable = observedLivePlayerPlayable?.let {
        val observedPlayerPlayable by it.livePlayable.observeAsState()
        val playerPlayable = observedPlayerPlayable ?: return@let false
        return@let playerPlayable.mediaItemId == playable.mediaItemId
    } ?: false

    val variant = when {
        playableIsPlayerPlayable -> when (playerState) {
            is Player.State.Paused,
            is Player.State.Error,
            is Player.State.Idle -> PlaybackButtonVariant.PLAY
            is Player.State.Playing -> PlaybackButtonVariant.PAUSE
            is Player.State.Loading -> PlaybackButtonVariant.LOADING
        }
        else -> PlaybackButtonVariant.PLAY
    }

    val action = actionForVariant(
        variant,
        player = playerViewModel.player,
        playable,
        resume = playableIsPlayerPlayable,
    )

    PlaybackButton(style, variant, modifier, action)
}

@Composable
fun PlaybackButton(
    style: PlaybackButtonStyle,
    variant: PlaybackButtonVariant,
    modifier: Modifier,
    action: () -> Unit,
) {
    when (style) {
        PlaybackButtonStyle.CIRCLE -> PlaybackCircleButton(variant, modifier, action)
        PlaybackButtonStyle.LIVE -> PlaybackLiveButton(variant, modifier, action)
        PlaybackButtonStyle.PLAIN -> PlaybackPlainButton(variant, modifier, action)
        PlaybackButtonStyle.NEW_CIRCLE -> PlaybackNewCircleButton(variant, modifier, action)
    }
}

@Composable
fun PlaybackNewCircleButton(variant: PlaybackButtonVariant, modifier: Modifier, action: () -> Unit) {

    val colors = ButtonDefaults.buttonColors(
        backgroundColor = Color.White,
        contentColor = Color.Black,
        disabledBackgroundColor = Color.White,
        disabledContentColor = Color.Black,
    )

    Button(
        onClick = action,
        modifier = modifier,
        enabled = variant != PlaybackButtonVariant.LOADING,
        shape = CircleShape,
        colors = colors,
        contentPadding = PaddingValues(0.dp)
    ) {
        when (variant) {
            PlaybackButtonVariant.PLAY -> {
                val xOffsetFraction = .05F
                Spacer(modifier = Modifier.fillMaxWidth(xOffsetFraction))
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxSize(fraction = .45F),
                )
            }
            PlaybackButtonVariant.PAUSE ->
                Icon(
                    painter = painterResource(id = R.drawable.ic_pause),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxSize(fraction = .45F),
                )
            PlaybackButtonVariant.LOADING ->
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.fillMaxSize(fraction = .45F)
                )
        }
    }

}

@Composable
private fun PlaybackCircleButton(variant: PlaybackButtonVariant, modifier: Modifier, action: () -> Unit) {

    val colors = ButtonDefaults.buttonColors(
        backgroundColor = Color.Black,
        contentColor = Color.White,
        disabledBackgroundColor = Color.Black,
        disabledContentColor = Color.White,
    )

    Button(
        onClick = action,
        modifier = modifier,
        enabled = variant != PlaybackButtonVariant.LOADING,
        shape = CircleShape,
        colors = colors,
        contentPadding = PaddingValues(0.dp)
    ) {
        when (variant) {
            PlaybackButtonVariant.PLAY,
            PlaybackButtonVariant.PAUSE ->
                Icon(
                    imageVector = iconForVariant(variant),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxSize(fraction = .7F),
                )
            PlaybackButtonVariant.LOADING ->
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.fillMaxSize(fraction = .5F)
                )
        }
    }
}

@Composable
private fun PlaybackLiveButton(
    variant: PlaybackButtonVariant,
    modifier: Modifier = Modifier,
    action: () -> Unit
) {

    val colors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.onBackground,
        contentColor = MaterialTheme.colors.background,
        disabledBackgroundColor = MaterialTheme.colors.onBackground,
        disabledContentColor = MaterialTheme.colors.background,
    )

    Button(
        onClick = action,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(0.dp),
        colors = colors,
        enabled = variant != PlaybackButtonVariant.LOADING,
        modifier = modifier
            .size(width = 76.dp, height = 30.dp),
    ) {
        when (variant) {
            PlaybackButtonVariant.PLAY -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxHeight(.45F)
                        .aspectRatio(1F),
                )
                Spacer(modifier = Modifier.fillMaxWidth(.1F))
            }
            PlaybackButtonVariant.PAUSE -> {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pause),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxHeight(.45F)
                        .aspectRatio(1F),
                )
                Spacer(modifier = Modifier.fillMaxWidth(.1F))
            }
            PlaybackButtonVariant.LOADING -> {
                Spacer(modifier = Modifier.fillMaxWidth(.03F))
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxHeight(.68F)
                        .aspectRatio(1F)
                        .padding(4.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.fillMaxWidth(.04F))
            }
        }
        Text(
            maxLines = 1,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            ),
            text = "LIVE",
            modifier = Modifier
                .offset(y = (-.5).dp)
        )
        if (variant == PlaybackButtonVariant.LOADING) {
            Spacer(modifier = Modifier.fillMaxWidth(.21F))
        }
    }
}

@Composable
private fun PlaybackPlainButton(
    variant: PlaybackButtonVariant,
    modifier: Modifier = Modifier,
    action: () -> Unit,
) {

    val colors = ButtonDefaults.buttonColors(
        backgroundColor = Color.Transparent,
        contentColor = Color.White,
        disabledBackgroundColor = Color.Transparent,
        disabledContentColor = Color.White,
    )

    Button(
        onClick = action,
        modifier = modifier,
        colors = colors,
        enabled = variant != PlaybackButtonVariant.LOADING,
        elevation = null,
        contentPadding = PaddingValues(0.dp),
    ) {
        when (variant) {
            PlaybackButtonVariant.PLAY -> {
                val xOffsetFraction = .02F
                Spacer(modifier = Modifier.fillMaxWidth(xOffsetFraction))
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxSize(.4F),
                )
            }
            PlaybackButtonVariant.PAUSE ->
                Icon(
                    painter = painterResource(id = R.drawable.ic_pause),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxSize(.4F),
                )
            PlaybackButtonVariant.LOADING ->
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxSize(.4F)
                )
        }
    }
}

private fun iconForVariant(variant: PlaybackButtonVariant): ImageVector = when (variant) {
    PlaybackButtonVariant.PAUSE -> Icons.Filled.Pause
    PlaybackButtonVariant.PLAY -> Icons.Filled.PlayArrow
    PlaybackButtonVariant.LOADING -> Icons.Filled.Downloading
}

private fun descriptionForVariant(variant: PlaybackButtonVariant): String = when (variant) {
    PlaybackButtonVariant.PAUSE -> "Pause"
    PlaybackButtonVariant.PLAY -> "Play"
    PlaybackButtonVariant.LOADING -> "Loading"
}

private fun actionForVariant(
    variant: PlaybackButtonVariant,
    player: Player,
    playable: Playable,
    resume: Boolean,
): () -> Unit = when (variant) {
    PlaybackButtonVariant.LOADING ->
        ({})
    PlaybackButtonVariant.PAUSE ->
        ({ player.pause() })
    PlaybackButtonVariant.PLAY ->
        if (resume) ({ player.play() })
        else ({ player.play(playable) })
}

@Preview
@Composable
private fun PlaybackButtonCirclePreviews() {
    val action = ({})
    Box(
        modifier = Modifier
            .background(Color.DarkGray)
            .size(width = 250.dp, height = 100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackCircleButton(variant = PlaybackButtonVariant.PLAY, modifier = Modifier.size(60.dp), action = action)
            PlaybackCircleButton(variant = PlaybackButtonVariant.PAUSE, modifier = Modifier.size(60.dp), action = action)
            PlaybackCircleButton(variant = PlaybackButtonVariant.LOADING, modifier = Modifier.size(60.dp), action = action)
        }
    }
}

@Preview
@Composable
private fun PlaybackButtonNewCirclePreviews() {
    val action = ({})
    Box(
        modifier = Modifier
            .background(Color.DarkGray)
            .size(width = 250.dp, height = 100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackNewCircleButton(variant = PlaybackButtonVariant.PLAY, modifier = Modifier.size(60.dp), action = action)
            PlaybackNewCircleButton(variant = PlaybackButtonVariant.PAUSE, modifier = Modifier.size(60.dp), action = action)
            PlaybackNewCircleButton(variant = PlaybackButtonVariant.LOADING, modifier = Modifier.size(60.dp), action = action)
        }
    }
}

@Preview
@Composable
private fun PlaybackButtonLivePreviews() {
    val action = ({})
    Box(
        modifier = Modifier
            .background(Color.DarkGray)
            .size(width = 100.dp, height = 130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PlaybackLiveButton(variant = PlaybackButtonVariant.PLAY, action = action)
            PlaybackLiveButton(variant = PlaybackButtonVariant.PAUSE, action = action)
            PlaybackLiveButton(variant = PlaybackButtonVariant.LOADING, action = action)
        }
    }
}

@Preview
@Composable
private fun PlaybackButtonPlainPreviews() {
    val action = ({})
    val buttonModifier = Modifier
        .size(width = 60.dp, height = 60.dp)
        .border(width = 1.dp, brush = SolidColor(Color.Blue), shape = RectangleShape)
    Box(
        modifier = Modifier
            .background(Color.DarkGray)
            .size(width = 250.dp, height = 100.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackPlainButton(variant = PlaybackButtonVariant.PLAY, modifier = buttonModifier, action = action)
            PlaybackPlainButton(variant = PlaybackButtonVariant.PAUSE, modifier = buttonModifier, action = action)
            PlaybackPlainButton(variant = PlaybackButtonVariant.LOADING, modifier = buttonModifier, action = action)
        }
    }
}

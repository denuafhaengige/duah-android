package dk.denuafhaengige.android.views

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.denuafhaengige.android.models.Employee
import dk.denuafhaengige.android.player.Playable
import dk.denuafhaengige.android.player.Player
import dk.denuafhaengige.android.player.PlayerViewModel
import dk.denuafhaengige.android.theming.RedColor
import dk.denuafhaengige.android.util.LivePlayable

enum class PlaybackButtonStyle {
    PLAIN,
    CIRCLE,
    LIVE,
}

private enum class PlaybackButtonVariant {
    PLAY,
    PAUSE,
    LOADING,
}

@Composable
fun PlaybackButton(
    playerViewModel: PlayerViewModel,
    playable: Playable,
    style: PlaybackButtonStyle,
    modifier: Modifier,
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

    when (style) {
        PlaybackButtonStyle.CIRCLE -> PlaybackCircleButton(variant, modifier, action)
        PlaybackButtonStyle.LIVE -> PlaybackLiveButton(variant, modifier, action)
        PlaybackButtonStyle.PLAIN -> PlaybackPlainButton(variant, modifier, action)
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
private fun PlaybackLiveButton(variant: PlaybackButtonVariant, modifier: Modifier, action: () -> Unit) {

    val colors = ButtonDefaults.buttonColors(
        backgroundColor = RedColor,
        contentColor = Color.White,
        disabledBackgroundColor = RedColor,
        disabledContentColor = Color.White,
    )

    Button(
        onClick = action,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp),
        colors = colors,
        enabled = variant != PlaybackButtonVariant.LOADING,
        modifier = modifier,
    ) {
        when (variant) {
            PlaybackButtonVariant.PLAY,
            PlaybackButtonVariant.PAUSE ->
                Icon(
                    imageVector = iconForVariant(variant),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxHeight(.6F)
                        .aspectRatio(1F)
                        .offset(x = (-4).dp),
                )
            PlaybackButtonVariant.LOADING ->
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxHeight(.6F)
                        .aspectRatio(1F)
                        .offset(x = (-4).dp)
                        .padding(4.dp),
                    strokeWidth = 2.dp,
                )
        }
        Text(
            maxLines = 1,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            ),
            text = "LIVE",
        )
    }
}

@Composable
private fun PlaybackPlainButton(variant: PlaybackButtonVariant, modifier: Modifier, action: () -> Unit) {

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
            PlaybackButtonVariant.PLAY,
            PlaybackButtonVariant.PAUSE ->
                Icon(
                    imageVector = iconForVariant(variant),
                    contentDescription = descriptionForVariant(variant),
                    modifier = Modifier
                        .fillMaxSize(.7F),
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
private fun PlaybackButtonLivePreviews() {
    val action = ({})
    val buttonModifier = Modifier
        .size(width = 80.dp, height = 30.dp)
    Box(
        modifier = Modifier
            .background(Color.DarkGray)
            .size(width = 300.dp, height = 80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackLiveButton(variant = PlaybackButtonVariant.PLAY, modifier = buttonModifier, action = action)
            PlaybackLiveButton(variant = PlaybackButtonVariant.PAUSE, modifier = buttonModifier, action = action)
            PlaybackLiveButton(variant = PlaybackButtonVariant.LOADING, modifier = buttonModifier, action = action)
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

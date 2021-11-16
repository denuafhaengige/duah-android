package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.insets.statusBarsHeight
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.theming.LighterDarkGrey
import com.denuafhaengige.duahandroid.theming.VeryDarkerGrey
import com.denuafhaengige.duahandroid.theming.VeryLighterGrey
import com.denuafhaengige.duahandroid.util.LiveEntity

@Composable
fun DynamicLogoTopAppBar(
    playerViewModel: PlayerViewModel,
    navController: NavController,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>?,
) {
    LogoTopAppBar(
        leftBarItem = {
            DynamicLogoTopAppBarBackButton(
                navController = navController
            )
        },
        rightBarItem = {
            liveChannel?.let {
                LogoTopAppBarLiveButton(
                    playerViewModel = playerViewModel,
                    liveChannel = liveChannel,
                )
            }
        }
    )
}

@Composable
fun LogoTopAppBar(
    leftBarItem: @Composable () -> Unit,
    rightBarItem: @Composable () -> Unit,
) {

    val borderColor = LighterDarkGrey

    val logoPainter =
        if (isSystemInDarkTheme()) painterResource(id = R.drawable.round_icon_black_on_white)
        else painterResource(id = R.drawable.round_icon_white_on_black)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsHeight()
                .background(color = appBarBackgroundColor()),
        )
        Box(
            modifier = Modifier
        ) {
            TopAppBar(
                backgroundColor = appBarBackgroundColor(),
                contentPadding = PaddingValues(0.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawAppBarBorderBehind(AppBarBorderVariant.BOTTOM)
                        .padding(horizontal = 8.dp),
                ) {
                    leftBarItem()
                    Spacer(modifier = Modifier)
                    rightBarItem()
                }
            }
            Image(
                painter = logoPainter,
                contentDescription = null, // decorative element
                modifier = Modifier
                    .size(56.dp)
                    .scale(1.15F)
                    .align(alignment = Alignment.Center)
            )
        }
    }
}

@Composable
fun DynamicLogoTopAppBarBackButton(navController: NavController) {

    val backTrackEntry by navController.currentBackStackEntryAsState()
    var canNavigateUp by remember { mutableStateOf(false) }

    LaunchedEffect(backTrackEntry) {
        canNavigateUp = navController.previousBackStackEntry != null
    }

    if (!canNavigateUp) {
        return
    }

    LogoTopAppBarBackButton(
        action = {
            if (navController.previousBackStackEntry != null) {
                navController.popBackStack()
            }
        },
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F),
    )
}

@Composable
fun LogoTopAppBarBackButton(
    action: () -> Unit,
    modifier: Modifier = Modifier,
) {

    IconButton(
        onClick = action,
        modifier = modifier,
    ) {
        Icon(
            painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
            contentDescription = "Back",
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        )
    }
}

@Composable
fun LogoTopAppBarLiveButton(
    playerViewModel: PlayerViewModel,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>,
) {

    val channel by liveChannel.liveEntity.observeAsState()

    channel?.let {
        Box(modifier = Modifier.padding(end = 5.dp)) {
            DynamicPlaybackButton(
                playerViewModel = playerViewModel,
                playable = Playable.Channel(channel = it),
                style = PlaybackButtonStyle.LIVE,
            )
        }
    }
}

@Composable
private fun LogoTopAppBarLiveButtonPreview() {
    Box(modifier = Modifier.padding(end = 5.dp)) {
        PlaybackButton(
            style = PlaybackButtonStyle.LIVE,
            modifier = Modifier.size(width = 80.dp, height = 40.dp),
            action = {},
            variant = PlaybackButtonVariant.PLAY
        )
    }
}

@Preview
@Composable
private fun LogoTopAppBarPreview() {

    LogoTopAppBar(
        leftBarItem = {
            LogoTopAppBarBackButton(
                action = {},
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1F),
            )
        },
        rightBarItem = {
            LogoTopAppBarLiveButtonPreview()
        }
    )
}

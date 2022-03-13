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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.members.MembersViewModel
import com.google.accompanist.insets.statusBarsHeight
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LiveEntity

@Composable
fun DynamicLogoTopAppBar(
    membersViewModel: MembersViewModel,
    playerViewModel: PlayerViewModel,
    navController: NavController,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>?,
) {
    val backTrackEntry by navController.currentBackStackEntryAsState()
    var canNavigateUp by remember { mutableStateOf(false) }

    LaunchedEffect(backTrackEntry) {
        canNavigateUp = navController.previousBackStackEntry != null
    }

    LogoTopAppBar(
        leftBarItem = {
            if (canNavigateUp) {
                DynamicLogoTopAppBarBackButton(navController)
            } else {
                LogoTopAppBarMemberButton(membersViewModel)
            }
        },
        rightBarItem = {
            liveChannel?.let {
                LogoTopAppBarLiveButton(
                    membersViewModel = membersViewModel,
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

    val logoPainter =
        if (isSystemInDarkTheme()) painterResource(id = R.drawable.round_icon_black_on_white)
        else painterResource(id = R.drawable.round_icon_white_on_black)

    val topAppBarHeight = 50.dp

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsHeight(additional = 10.dp)
                .background(color = appBarBackgroundColor()),
        )
        Box(
            modifier = Modifier
        ) {
            TopAppBar(
                backgroundColor = appBarBackgroundColor(),
                contentPadding = PaddingValues(0.dp),
                elevation = 0.dp,
                modifier = Modifier
                    .height(topAppBarHeight),
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
                    .size(topAppBarHeight)
                    .scale(1.15F)
                    .align(alignment = Alignment.Center)
            )
        }
    }
}

@Composable
fun DynamicLogoTopAppBarBackButton(navController: NavController) {

    LogoTopAppBarBackButton {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }
}

@Composable
fun LogoTopAppBarBackButton(
    modifier: Modifier = Modifier,
    action: () -> Unit = {},
) {

    IconButton(
        onClick = action,
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1F),
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
fun LogoTopAppBarMemberButton(
    membersViewModel: MembersViewModel,
) {
    DynamicMemberButton(
        membersViewModel = membersViewModel,
        modifier = Modifier.padding(start = 5.dp),
    )
}

@Composable
private fun LogoTopAppBarMemberButtonPreview(
    state: MemberButtonState
) {
    MemberButton(
        state = state,
        modifier = Modifier.padding(start = 5.dp),
    )
}

@Composable
fun LogoTopAppBarLiveButton(
    membersViewModel: MembersViewModel,
    playerViewModel: PlayerViewModel,
    liveChannel: LiveEntity<ChannelWithCurrentBroadcast>,
) {

    val channel by liveChannel.liveEntity.observeAsState()

    channel?.let {
        Box(modifier = Modifier.padding(end = 5.dp)) {
            DynamicPlaybackButton(
                membersViewModel = membersViewModel,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        LogoTopAppBar(
            leftBarItem = {
                LogoTopAppBarMemberButtonPreview(state = MemberButtonState.NOT_LOGGED_IN)
            },
            rightBarItem = {
                LogoTopAppBarLiveButtonPreview()
            }
        )
        LogoTopAppBar(
            leftBarItem = {
                LogoTopAppBarMemberButtonPreview(state = MemberButtonState.LOGGED_IN)
            },
            rightBarItem = {
                LogoTopAppBarLiveButtonPreview()
            }
        )
        LogoTopAppBar(
            leftBarItem = {
                LogoTopAppBarBackButton()
            },
            rightBarItem = {
                LogoTopAppBarLiveButtonPreview()
            }
        )
    }
}

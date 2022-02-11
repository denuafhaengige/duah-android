package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.BroadcastsFilter
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees

@Composable
fun DynamicBroadcastList(
    viewModel: AppViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    filter: BroadcastsFilter? = null,
) {
    var broadcasts by remember {
        mutableStateOf<List<BroadcastWithProgramAndEmployees>?>(null)
    }

    LaunchedEffect(filter) {
        broadcasts = viewModel.broadcasts(filter)
    }

    if (broadcasts == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
        return
    }

    broadcasts?.let { nonNullBroadcasts ->
        BroadcastsList(
            broadcasts = nonNullBroadcasts,
            modifier = modifier,
            broadcastClickedAction = { broadcast ->
                navController.navigate(NavigationRouteDest.Broadcast(broadcast).destRoute)
            },
            broadcastVisualContent = { broadcast ->
                DynamicBroadcastVisualPlayButton(
                    playerViewModel = viewModel.playerViewModel,
                    broadcast = broadcast,
                )
            }
        )
    }
}

@Composable
fun BroadcastsList(
    broadcasts: List<BroadcastWithProgramAndEmployees>,
    modifier: Modifier = Modifier,
    broadcastClickedAction: (broadcast: BroadcastWithProgramAndEmployees) -> Unit = {},
    broadcastVisualContent: @Composable (broadcast: BroadcastWithProgramAndEmployees) -> Unit = {},
) {

    val lazyState = rememberLazyListState()

    LazyColumn(
        state = lazyState,
        modifier = modifier,
    ) {
        itemsIndexed(broadcasts) { index, broadcast ->

            val backgroundColor =
                if (index % 2 == 0) MaterialTheme.colors.background
                else MaterialTheme.colors.surface

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .clickable { broadcastClickedAction(broadcast) }
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                BroadcastVisual(
                    broadcast = broadcast,
                    style = BroadcastVisualStyle.SQUARE,
                    modifier = Modifier
                        .size(ContentDimensions.squareBannerSize),
                    content = { broadcastVisualContent(broadcast) },
                )
                Spacer(
                    modifier = Modifier
                        .width(20.dp),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .padding(vertical = 5.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        MetaTitleTextForContent(broadcast)
                        MetaTitleSupplementTextForContent(broadcast)
                    }
                    SmallTitleTextForContent(broadcast)
                }
            }
        }
    }

}

@Preview
@Composable
fun BroadcastListPreview() {
    BroadcastsList(
        broadcasts = listOf(
            BroadcastWithProgramAndEmployees.example,
            BroadcastWithProgramAndEmployees.example,
            BroadcastWithProgramAndEmployees.example,
            BroadcastWithProgramAndEmployees.example,
            BroadcastWithProgramAndEmployees.example,
            BroadcastWithProgramAndEmployees.example,
            BroadcastWithProgramAndEmployees.example,
        ),
        modifier = Modifier
            .fillMaxSize(),
    )
}
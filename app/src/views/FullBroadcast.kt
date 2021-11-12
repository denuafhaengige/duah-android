package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.content.EntityFlow
import com.denuafhaengige.duahandroid.models.BroadcastFetcher
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees
import com.denuafhaengige.duahandroid.util.LiveEntity

@Composable
fun DynamicFullBroadcastById(viewModel: AppViewModel, broadcastId: Int) {

    var liveBroadcast by remember { mutableStateOf<LiveEntity<BroadcastWithProgramAndEmployees>?>(null) }

    LaunchedEffect(broadcastId) {
        val contentStore = Application.contentProvider.contentStore
        val broadcastDao = contentStore.database.broadcastDao()
        val foundBroadcasts = broadcastDao.loadAllByIds(arrayOf(broadcastId).toIntArray())
        val broadcast =
            if (foundBroadcasts.isNotEmpty()) foundBroadcasts.first()
            else null
        if (broadcast == null) {
            return@LaunchedEffect
        }
        liveBroadcast = LiveEntity(
            entityFlow = EntityFlow(
                entity = broadcast,
                contentStoreEventFlow = contentStore.eventFlow,
                fetcher = BroadcastFetcher(store = contentStore),
            )
        )
    }

    liveBroadcast?.let {
        DynamicFullBroadcast(
            viewModel = viewModel,
            liveBroadcast = it,
        )
    }
}

@Composable
fun DynamicFullBroadcast(viewModel: AppViewModel, liveBroadcast: LiveEntity<BroadcastWithProgramAndEmployees>) {
    
    val observedBroadcast by liveBroadcast.liveEntity.observeAsState()
    val broadcast = observedBroadcast ?: return
    
    FullBroadcast(
        broadcast = broadcast,
        topVisualContent = {
            DynamicBroadcastVisualPlayButton(
                playerViewModel = viewModel.playerViewModel,
                broadcast = broadcast,
            )
        }
    )
}

@Composable
fun FullBroadcast(
    broadcast: BroadcastWithProgramAndEmployees,
    topVisualContent: @Composable () -> Unit = {},
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        BroadcastVisual(
            broadcast = broadcast,
            style = BroadcastVisualStyle.WIDE,
            modifier = Modifier
                .fillMaxWidth()
                .height(ContentDimensions.wideBannerHeight),
        ) {
            topVisualContent()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            MetaTextForContent(content = broadcast)
            LargeTitleTextForContent(content = broadcast)
            DescriptionTextForContent(content = broadcast)
        }
    }

}

@Preview
@Composable
private fun FullBroadcastPreview() {
    FullBroadcast(
        broadcast = BroadcastWithProgramAndEmployees.example,
        topVisualContent = {
            BroadcastVisualPlayButton { verticalPadding, horizontalPadding, diameter ->
                PlaybackButton(
                    style = PlaybackButtonStyle.NEW_CIRCLE,
                    variant = PlaybackButtonVariant.PLAY,
                    modifier = Modifier
                        .padding(vertical = verticalPadding, horizontal = horizontalPadding)
                        .size(diameter),
                    action = {},
                )
            }
        }
    )
}

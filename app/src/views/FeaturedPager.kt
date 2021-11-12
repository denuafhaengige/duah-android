package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.denuafhaengige.duahandroid.content.Featured
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LiveFeatured
import com.denuafhaengige.duahandroid.util.capitalizeWords

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FeaturedPager(
    content: List<LiveFeatured>,
    playerViewModel: PlayerViewModel,
    navController: NavController,
) {

    val state = rememberPagerState(pageCount = content.size)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(365.dp),
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPagerIndicator(
                pagerState = state,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
        HorizontalPager(
            state,
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val item = content[page]
            FeaturedPagerItem(
                liveFeatured = item,
                playerViewModel = playerViewModel,
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FeaturedPagerItem(
    liveFeatured: LiveFeatured,
    playerViewModel: PlayerViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {

    val observedFeatured by liveFeatured.liveFeatured.observeAsState()
    val featured = observedFeatured ?: return

    val navAction = when (featured) {
        is Featured.Broadcast -> ({
            val destRoute =
                NavigationRouteDest.Broadcast(id = featured.entity.id).destRoute
            navController.navigate(route = destRoute)
        })
        else -> ({})
    }

    Column(
        modifier = modifier
            .clickable { navAction() },
    ) {
        when (featured) {
            is Featured.Broadcast ->
                BroadcastVisual(
                    broadcast = featured.entity,
                    style = BroadcastVisualStyle.WIDE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ContentDimensions.wideBannerHeight),
                ) {
                    DynamicBroadcastVisualPlayButton(
                        playerViewModel = playerViewModel,
                        broadcast = featured.entity,
                    )
                }
            else -> {}
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            MetaTextForContent(content = featured)
            LargeTitleTextForContent(content = featured, maxLines = 2)
            DescriptionTextForContent(content = featured, maxLines = 1)
        }
    }
}

@Preview
@Composable
fun FeaturedPagerItemPreview() {
    //FeaturedPagerItem(content = Featured.Broadcast(entity = BroadcastWithProgramAndEmployees.example))
}

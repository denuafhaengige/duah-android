package com.denuafhaengige.duahandroid.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.models.BroadcastFetcher

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Home(viewModel: AppViewModel, navController: NavController) {

    val featuredContent by viewModel.featuredContent.observeAsState()
    val latestBroadcasts by viewModel.latestBroadcasts.observeAsState()
    val programs by viewModel.programs.observeAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {

        featuredContent?.let {
            FeaturedPager(
                content = it,
                membersViewModel = viewModel.membersViewModel,
                playerViewModel = viewModel.playerViewModel,
                navController = navController,
            )
        }

        latestBroadcasts?.let {
            LatestBroadcasts(
                playableBroadcasts = it,
                membersViewModel = viewModel.membersViewModel,
                playerViewModel = viewModel.playerViewModel,
                navController = navController,
            )
        }

        programs?.let {
            DynamicProgramsContentRow(
                programs = it,
                navController = navController,
            )
        }
    }
    
    
}

package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.protobuf.StringValue
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.denuafhaengige.duahandroid.AppViewModel

interface SpecRoute {
    val specRoute: String
}

interface HasNavArgs {
    val navArgs: List<NamedNavArgument>
}

interface DestRoute {
    val destRoute: String
}

sealed class NavigationRouteSpec: SpecRoute, HasNavArgs {

    object Home: NavigationRouteSpec() {
        override val specRoute: String
            get() = "home"
        override val navArgs: List<NamedNavArgument>
            get() = emptyList()
    }

    object Broadcast: NavigationRouteSpec() {

        const val argNameBroadcastId = "broadcastId"

        override val specRoute: String
            get() = "broadcast/{$argNameBroadcastId}"
        override val navArgs: List<NamedNavArgument>
            get() = listOf(navArgument(argNameBroadcastId) { type = NavType.IntType } )
    }
}

sealed class NavigationRouteDest: DestRoute {

    object Home: NavigationRouteDest() {
        override val destRoute: String
            get() = NavigationRouteSpec.Home.specRoute
    }

    data class Broadcast(val id: Int): NavigationRouteDest() {
        override val destRoute: String
            get() = NavigationRouteSpec.Broadcast.specRoute.replace(
                oldValue = "{${NavigationRouteSpec.Broadcast.argNameBroadcastId}}",
                newValue = "$id"
            )
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {

    NavHost(
        navController = navController,
        startDestination = NavigationRouteDest.Home.destRoute,
        modifier = modifier,
    ) {
        composable(route = NavigationRouteSpec.Home.specRoute) {
            Home(viewModel = viewModel, navController = navController)
        }
        composable(
            route = NavigationRouteSpec.Broadcast.specRoute,
            arguments = NavigationRouteSpec.Broadcast.navArgs,
        ) { navBackStackEntry ->
            val broadcastId = navBackStackEntry.arguments?.getInt(
                NavigationRouteSpec.Broadcast.argNameBroadcastId
            )
            broadcastId?.let {
                DynamicFullBroadcastById(viewModel = viewModel, broadcastId = it)
            }
        }
    }

}

package com.denuafhaengige.duahandroid.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.BroadcastsFilter
import com.denuafhaengige.duahandroid.models.Identifiable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import java.net.URLDecoder
import java.net.URLEncoder

interface SpecRoute {
    val specRoute: String
}

interface HasNavArgs {
    val navArgs: List<NamedNavArgument>
}

interface DestRoute {
    val destRoute: String
}

private object NavigationUtils {

    val adapter: JsonAdapter<List<Int>> by lazy {
        val type = Types.newParameterizedType(List::class.java, Integer::class.java)
        Application.moshi.adapter(type)
    }

    fun intListToNavArgs(value: List<Int>): String {
        val serialized = adapter.toJson(value)
        return URLEncoder.encode(serialized, "UTF-8")
    }

    fun navArgToIntList(value: String): List<Int>? {
        val decoded = URLDecoder.decode(value, "UTF-8")
        return adapter.fromJson(decoded)
    }
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

    object BroadcastsList: NavigationRouteSpec() {

        const val argNameHostIds = "hostIds"
        const val argNameProgramIds = "programIds"

        override val specRoute: String
            get() = "broadcasts?$argNameHostIds={$argNameHostIds}&$argNameProgramIds={$argNameProgramIds}"
        override val navArgs: List<NamedNavArgument>
            get() = listOf(
                navArgument(argNameHostIds) {
                    nullable = true
                    type = NavType.StringType
                },
                navArgument(argNameProgramIds) {
                    nullable = true
                    type = NavType.StringType
                },
            )
    }
}

sealed class NavigationRouteDest: DestRoute {

    object Home: NavigationRouteDest() {
        override val destRoute: String
            get() = NavigationRouteSpec.Home.specRoute
    }

    data class Broadcast(val broadcast: Identifiable): NavigationRouteDest() {
        override val destRoute: String
            get() = NavigationRouteSpec.Broadcast.specRoute.replace(
                oldValue = "{${NavigationRouteSpec.Broadcast.argNameBroadcastId}}",
                newValue = "${broadcast.id}"
            )
    }

    data class BroadcastsList(val filter: BroadcastsFilter? = null): NavigationRouteDest() {
        override val destRoute: String
            get() {
                val hostIdsNavArg =
                    filter?.hostIds?.let { NavigationUtils.intListToNavArgs(it) } ?: "null"
                val programIdsNavArg =
                    filter?.programIds?.let { NavigationUtils.intListToNavArgs(it) } ?: "null"
                return NavigationRouteSpec.BroadcastsList.specRoute
                    .replace(
                        oldValue = "{${NavigationRouteSpec.BroadcastsList.argNameHostIds}}",
                        newValue = hostIdsNavArg,
                    )
                    .replace(
                        oldValue = "{${NavigationRouteSpec.BroadcastsList.argNameProgramIds}}",
                        newValue = programIdsNavArg,
                    )
            }
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
        composable(
            route = NavigationRouteSpec.Home.specRoute,
            arguments = NavigationRouteSpec.Home.navArgs,
        ) {
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
        composable(
            route = NavigationRouteSpec.BroadcastsList.specRoute,
            arguments = NavigationRouteSpec.BroadcastsList.navArgs,
        ) { navBackStackEntry ->
            val hostIdsArgName = NavigationRouteSpec.BroadcastsList.argNameHostIds
            val hostIds = navBackStackEntry.arguments?.getString(hostIdsArgName)?.let {
                NavigationUtils.navArgToIntList(it)
            }
            val programIdsArgName = NavigationRouteSpec.BroadcastsList.argNameProgramIds
            val programIds = navBackStackEntry.arguments?.getString(programIdsArgName)?.let {
                NavigationUtils.navArgToIntList(it)
            }
            val filter =
                if (hostIds != null || programIds != null) BroadcastsFilter(programIds, hostIds)
                else null
            DynamicBroadcastList(
                viewModel = viewModel,
                navController = navController,
                filter = filter,
            )
        }
    }

}

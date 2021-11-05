package dk.denuafhaengige.android.views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dk.denuafhaengige.android.AppState
import dk.denuafhaengige.android.AppViewModel

@Composable
fun App(viewModel: AppViewModel) {

    val appState by viewModel.appState.observeAsState()
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    val statusBarColor = Color.Transparent

    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = useDarkIcons,
        )
    }

    ProvideWindowInsets {
        when (appState) {
            is AppState.Ready -> Home(viewModel)
            is AppState.Loading -> Loading(viewModel)
            else -> Loading(viewModel)
        }
    }
}

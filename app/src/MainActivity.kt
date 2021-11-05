package dk.denuafhaengige.android

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import dk.denuafhaengige.android.player.*
import dk.denuafhaengige.android.content.ContentService
import dk.denuafhaengige.android.theming.DuahTheme
import dk.denuafhaengige.android.util.Log
import dk.denuafhaengige.android.util.Settings
import dk.denuafhaengige.android.views.App

class MainActivity : ComponentActivity() {

    private lateinit var player: Player
    private lateinit var appViewModel: AppViewModel
    private lateinit var settings: Settings

    // MARK: Life cycle


    override fun onCreate(savedInstanceState: Bundle?) {

        startForegroundService(Intent(this, ContentService::class.java))

        settings = Settings(context = applicationContext)
        player = Player(context = applicationContext, settings)

        appViewModel = ViewModelProvider(this, AppViewModel.Factory(settings, player))
            .get(AppViewModel::class.java)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DuahTheme {
                App(appViewModel)
            }
        }

        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        Log.debug("MainActivity | onStart")
        super.onStart()
    }

    override fun onPause() {
        Log.debug("MainActivity | onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.debug("MainActivity | onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.debug("MainActivity | onDestroy")
        super.onDestroy()
    }

}

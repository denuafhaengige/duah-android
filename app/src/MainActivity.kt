package com.denuafhaengige.duahandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.denuafhaengige.duahandroid.player.*
import com.denuafhaengige.duahandroid.theming.DuahTheme
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import com.denuafhaengige.duahandroid.views.App

class MainActivity : ComponentActivity() {

    private lateinit var player: Player
    private lateinit var appViewModel: AppViewModel
    private lateinit var settings: Settings

    // MARK: Life cycle


    override fun onCreate(savedInstanceState: Bundle?) {

        settings = Application.settings
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

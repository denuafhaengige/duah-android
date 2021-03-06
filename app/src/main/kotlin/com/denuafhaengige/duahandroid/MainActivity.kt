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
import timber.log.Timber

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
        Timber.d("MainActivity | onStart")
        super.onStart()
    }

    override fun onPause() {
        Timber.d("MainActivity | onPause")
        super.onPause()
    }

    override fun onStop() {
        Timber.d("MainActivity | onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Timber.d("MainActivity | onDestroy")
        super.onDestroy()
    }

}

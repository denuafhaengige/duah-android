package com.denuafhaengige.duahandroid

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.media2.common.*
import androidx.media2.session.*
import com.denuafhaengige.duahandroid.ui.theme.DenUafhængigeTheme

class MainActivity : ComponentActivity() {

    private lateinit var mediaController: MediaController
    private lateinit var radioEndpoint: Uri
    private lateinit var appViewModel: AppViewModel

    // MARK: Service connection

    private val mediaControllerCallbacks = object: MediaController.ControllerCallback() {

        override fun onConnected(
            controller: MediaController,
            allowedCommands: SessionCommandGroup
        ) {
            super.onConnected(controller, allowedCommands)
            refreshPlayerState()
        }

        override fun onPlayerStateChanged(controller: MediaController, state: Int) {
            super.onPlayerStateChanged(controller, state)
            refreshPlayerState()
        }
    }

    // MARK: Life cycle

    override fun onCreate(savedInstanceState: Bundle?) {

        println("MainActivity.kt onCreate()")

        super.onCreate(savedInstanceState)

        val sessionManager = MediaSessionManager.getInstance(this)
        val tokens = sessionManager.sessionServiceTokens
        println("MainActivity.kt onCreate() sessionServiceTokens: $tokens")

        val sessionToken = SessionToken(this, ComponentName(this, RadioService::class.java))
        println("MainActivity.kt onCreate() mediaToken: $sessionToken")
        mediaController = MediaController.Builder(this)
            .setSessionToken(sessionToken)
            .setControllerCallback(mainExecutor, mediaControllerCallbacks)
            .build()
        radioEndpoint = Uri.parse(getString(R.string.radio_endpoint))
        appViewModel = ViewModelProvider(this, AppViewModel.Factory(mediaController, radioEndpoint))
            .get(AppViewModel::class.java)

        setContent {
            DenUafhængigeTheme {
                App(appViewModel)
            }
        }
    }

    override fun onStart() {
        println("MainActivity.kt onStart()")
        super.onStart()
    }

    override fun onPause() {
        println("MainActivity.kt onPause()")
        super.onPause()
    }

    override fun onStop() {
        println("MainActivity.kt onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        println("MainActivity.kt onDestroy()")
        super.onDestroy()
        mediaController.close()
    }

    // MARK: Logic

    fun refreshPlayerState() {
        when (mediaController.playerState) {
            SessionPlayer.PLAYER_STATE_ERROR -> {
                println("MainActivity refreshPlayerState(): mediaController.playerState: PLAYER_STATE_ERROR")
                appViewModel.onPlaybackStateChange(AppViewModel.PlaybackState.PAUSED)
            }
            SessionPlayer.PLAYER_STATE_PAUSED -> {
                println("MainActivity refreshPlayerState(): mediaController.playerState: PLAYER_STATE_PAUSED")
                appViewModel.onPlaybackStateChange(AppViewModel.PlaybackState.PAUSED)
            }
            SessionPlayer.PLAYER_STATE_IDLE -> {
                println("MainActivity refreshPlayerState(): mediaController.playerState: PLAYER_STATE_IDLE")
                appViewModel.onPlaybackStateChange(AppViewModel.PlaybackState.PAUSED)
            }
            SessionPlayer.PLAYER_STATE_PLAYING -> {
                println("MainActivity refreshPlayerState(): mediaController.playerState: PLAYER_STATE_PLAYING")
                appViewModel.onPlaybackStateChange(AppViewModel.PlaybackState.PLAYING)
            }
        }
    }

}

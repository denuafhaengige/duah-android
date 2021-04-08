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

        override fun onPlayerStateChanged(controller: MediaController, state: Int) {
            super.onPlayerStateChanged(controller, state)
            when (state) {
                SessionPlayer.PLAYER_STATE_ERROR,
                SessionPlayer.PLAYER_STATE_PAUSED,
                SessionPlayer.PLAYER_STATE_IDLE ->
                    appViewModel.onPlaybackStateChange(AppViewModel.PlaybackState.PAUSED)
                SessionPlayer.PLAYER_STATE_PLAYING ->
                    appViewModel.onPlaybackStateChange(AppViewModel.PlaybackState.PLAYING)
            }
        }
    }

    // MARK: Life cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaController = MediaController.Builder(this)
            .setSessionToken(SessionToken(this, ComponentName(this, RadioService::class.java)))
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

    override fun onStop() {
        super.onStop()
        mediaController.close()
    }

}

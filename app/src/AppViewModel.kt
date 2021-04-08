package com.denuafhaengige.duahandroid

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media2.session.MediaController
import androidx.media2.session.SessionResult
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import com.github.michaelbull.result.*
import kotlinx.coroutines.MainScope

class AppViewModel(
    private val mediaController: MediaController,
    private val radioEndpoint: Uri,
) : ViewModel() {

    // MARK: Enums, Classes

    enum class PlaybackState {
        LOADING,
        PLAYING,
        PAUSED,
    }

    class Factory(
        private val mediaController: MediaController,
        private val radioEndpoint: Uri,
    ): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            if (!modelClass.isAssignableFrom(AppViewModel::class.java)) {
                throw IllegalArgumentException("Unknown ViewModel Class")
            }
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(mediaController, radioEndpoint) as T
        }
    }

    // MARK: UI Fields

    private val _playbackState = MutableLiveData(PlaybackState.PAUSED)
    val playbackState: LiveData<PlaybackState> = _playbackState

    // MARK: Events

    fun onPlaybackStateChange(newState: PlaybackState) {
        _playbackState.value = newState
    }

    fun onPlaybackButtonTapped() {
        MainScope().launch {
            when (playbackState.value) {
                PlaybackState.LOADING -> return@launch
                PlaybackState.PAUSED -> {
                    _playbackState.value = PlaybackState.LOADING
                    play()
                }
                PlaybackState.PLAYING -> {
                    _playbackState.value = PlaybackState.LOADING
                    stop()
                }
            }
        }
    }

    // MARK: Logic

    private suspend fun play(): Result<SessionResult, SessionResult> {
        val setMediaItemResult = mediaController
            .setMediaItem("$radioEndpoint")
            .await()
        if (setMediaItemResult.resultCode != SessionResult.RESULT_SUCCESS) {
            return Err(setMediaItemResult)
        }
        val playResult = mediaController
            .play()
            .await()
        if (playResult.resultCode != SessionResult.RESULT_SUCCESS) {
            return Err(playResult)
        }
        return Ok(playResult)
    }

    private suspend fun stop(): Result<SessionResult, SessionResult> {
        val pauseResult = mediaController
            .pause()
            .await()
        if (pauseResult.resultCode != SessionResult.RESULT_SUCCESS) {
            return Err(pauseResult)
        }
        return Ok(pauseResult)
    }

}
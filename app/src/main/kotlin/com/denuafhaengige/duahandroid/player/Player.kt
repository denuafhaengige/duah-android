package com.denuafhaengige.duahandroid.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player.*
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.content.ContentProvider
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.guava.await
import java.lang.ref.WeakReference
import kotlin.properties.Delegates
import androidx.media3.common.Player.Listener as PlayerListener

class Player (private val context: Context, private val settings: Settings) {

    // MARK: Types

    data class Error(val code: Code, val underlying: Any? = null) {
        enum class Code {
            FAILED_SETTING_MEDIA_ITEM,
            FAILED_STARTING_PLAYBACK,
            FAILED_PAUSING_PLAYBACK,
        }
    }

    enum class LoadingReason {
        STARTING,
        PAUSING,
        BUFFERING_TO_CATCH_UP,
        SEEKING,
    }

    sealed class State {
        object Idle : State()
        data class Loading(val reason: LoadingReason): State()
        object Paused: State()
        object Playing: State()
        data class Error(val error: Player.Error): State()
    }

    private sealed class InnerState {
        object Idle: InnerState()
        data class StartingPlayback(val retryCount: Int = 0): InnerState()
        object Playing: InnerState()
        object BufferingToCatchUp: InnerState()
        object PausingPlayback: InnerState()
        object Paused: InnerState()
        data class Seeking(val playImmediately: Boolean): InnerState()
        data class RetryingAfterError(val error: Player.Error, val retryCount: Int = 0): InnerState()
        data class Error(val error: Player.Error): InnerState()
    }

    // MARK: Props

    private var innerState: InnerState by Delegates.observable(InnerState.Idle) { _, oldValue, newValue ->
        Log.debug("Player | innerState | oldValue: $oldValue, newValue: $newValue")
        if (oldValue == newValue) {
            return@observable
        }
        when (newValue) {
            is InnerState.Idle -> _state.value = State.Idle
            is InnerState.RetryingAfterError,
            is InnerState.StartingPlayback ->
                State.Loading(LoadingReason.STARTING)
            is InnerState.PausingPlayback ->
                State.Loading(LoadingReason.PAUSING)
            is InnerState.BufferingToCatchUp -> _state.value =
                State.Loading(LoadingReason.BUFFERING_TO_CATCH_UP)
            is InnerState.Seeking -> _state.value =
                State.Loading(LoadingReason.SEEKING)
            is InnerState.Playing -> _state.value = State.Playing
            is InnerState.Paused -> _state.value = State.Paused
            is InnerState.Error -> _state.value = State.Error(error = newValue.error)
        }
    }

    private var observePosition: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        Log.debug("Player | observePosition | oldValue: $oldValue, newValue: $newValue")
        if (oldValue == newValue) {
            return@observable
        }
        scope.launch {
            while (observePosition) {
                withContext(scope.coroutineContext) {
                    _position.value = mediaController.currentPosition
                    withContext(backgroundScope.coroutineContext) { delay(500) }
                }
            }
        }
    }

   private val contentProvider
        get() = Application.contentProvider

    private val scope = CoroutineScope(Dispatchers.Main)
    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private val _connected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    val state = _state.asStateFlow()

    private val _playable: MutableStateFlow<PlayableFlow?> = MutableStateFlow(null)
    val playable = _playable.asStateFlow()

    private val _stream: MutableStateFlow<Stream?> = MutableStateFlow(null)
    val stream = _stream.asStateFlow()

    private val _duration: MutableStateFlow<Long?> = MutableStateFlow(null)
    val duration = _duration.asStateFlow()

    private val _position: MutableStateFlow<Long?> = MutableStateFlow(null)
    val position = _position.asStateFlow()

    // MARK: Public Interface

    fun play() = scope.launch {
        when (innerState) {
            is InnerState.Paused -> {
                innerPlay()
            }
            is InnerState.Error -> {
                val playable = playable.value?.flow?.value ?: return@launch
                play(playable)
            }
            else -> return@launch
        }
    }

    fun play(playable: Playable) = scope.launch {
        var stream = playable.preferredStreamWithSettings(settings) ?: return@launch
        val mediaItem = Media3Integration.mediaItemForPlayable(playable, settings) ?: return@launch
        _playable.value = PlayableFlow(playable, contentProvider.contentStore)
        _stream.value = stream
        mediaController.setMediaItem(mediaItem)
        innerPlay()
    }

    private fun innerPlay() {
        innerState = InnerState.StartingPlayback()
        val mediaControllerPlaybackState =
            Media3Integration.stringForPlaybackState(mediaController.playbackState)
        Log.debug("Player | innerPlay | mediaController.state: $mediaControllerPlaybackState")
        if (mediaController.playbackState == STATE_READY) {
            mediaController.play()
        } else {
            mediaController.prepare()
        }
    }

    fun pause() = scope.launch {
        if (innerState !is InnerState.Playing) {
            return@launch
        }
        innerState = InnerState.PausingPlayback
        mediaController.pause()
    }

    fun seek(target: Long) = scope.launch {
        if (innerState is InnerState.Seeking) {
            return@launch
        }
        observePosition = false
        innerState = InnerState.Seeking(playImmediately = innerState is InnerState.Playing)
        mediaController.seekTo(target)
    }

    // MARK: Init

    init {
        start()
    }

    private fun start() = scope.launch {
        scope.launch { wireMediaController() }
    }

    // MARK: MediaController

    private val mediaControllerCallbacks = object: MediaController.Listener {

        override fun onDisconnected(controller: MediaController) {
            super.onDisconnected(controller)
            Log.debug("Player | onDisconnected")
            _connected.value = false
        }
    }

    private lateinit var mediaController: MediaController

    // MARK: Logic

    private fun errorEncountered(error: Error) = when (error.code) {
        Error.Code.FAILED_SETTING_MEDIA_ITEM -> innerState = InnerState.Error(error)
        Error.Code.FAILED_STARTING_PLAYBACK -> innerState = InnerState.Error(error)
        Error.Code.FAILED_PAUSING_PLAYBACK -> innerState = InnerState.Error(error)
    }

    // MARK: Player.Listener

    @androidx.annotation.OptIn(UnstableApi::class)
    private val playerListener = object: PlayerListener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            val playbackStateString = Media3Integration.stringForPlaybackState(playbackState)
            Log.debug("PlayerService | onPlaybackStateChanged: $playbackStateString")
            innerState.let { atomicInnerState ->
                when {
                    playbackState == STATE_READY &&
                    atomicInnerState is InnerState.Seeking &&
                    !atomicInnerState.playImmediately -> {
                        _position.value = mediaController.currentPosition
                        innerState = InnerState.Paused
                    }
                    playbackState == STATE_READY &&
                    atomicInnerState is InnerState.StartingPlayback &&
                    stream.value?.type == StreamType.HLS_EVENT &&
                    mediaController.currentPosition != (0).toLong() -> {
                        mediaController.seekTo(0)
                    }
                    playbackState == STATE_READY &&
                    atomicInnerState is InnerState.StartingPlayback -> {
                        mediaController.play()
                    }
                }
            }
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            Log.debug("PlayerService | onIsLoadingChanged: $isLoading")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Log.debug("PlayerService | onIsPlayingChanged: $isPlaying")
            if (isPlaying) {
                _position.value = mediaController.currentPosition
                observePosition = true
                innerState = InnerState.Playing
                return
            }
            observePosition = false
            innerState = when (mediaController.playbackState) {
                STATE_BUFFERING -> {
                    if (innerState is InnerState.Seeking) innerState
                    else InnerState.BufferingToCatchUp
                }
                STATE_ENDED, STATE_IDLE -> InnerState.Idle
                STATE_READY -> InnerState.Paused
                else -> throw Throwable("Shut up")
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.debug("PlayerService | onPlayerError: $error")
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            Log.debug("PlayerService | onPlayerErrorChanged: $error")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            val stringValue = Media3Integration.stringForMediaItemTransitionReason(reason)
            Log.debug("PlayerService | onMediaItemTransition: $mediaItem, reason: $stringValue")
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            super.onTimelineChanged(timeline, reason)
            val reasonString = Media3Integration.stringForTimeLineChangeReason(reason)
            val duration = mediaController.duration
            val position = mediaController.currentPosition
            if (duration == C.TIME_UNSET) {
                _duration.value = null
                _position.value = null
            } else {
                _duration.value = duration
                _position.value = position
            }
            Log.debug("PlayerService | onTimelineChanged: $timeline, reason: $reasonString, duration: $duration, position: $position")
        }
    }

    // MARK: Plumbing

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun wireMediaController() = scope.launch {
        mediaController = MediaController.Builder(context, SessionToken(context, ComponentName(context, PlayerService::class.java)))
            .setListener(mediaControllerCallbacks)
            .buildAsync()
            .await()
        mediaController.addListener(playerListener)
        _connected.value = true
        innerState = InnerState.Idle
        // TODO: Derive state because Player class might be instantiated while media session is already ongoing?
    }

}

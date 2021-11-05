package com.denuafhaengige.duahandroid.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denuafhaengige.duahandroid.util.LivePlayable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class PlayerViewModel(val player: Player) {

    // MARK: Props

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _playable: MutableLiveData<LivePlayable?> =
        if (player.playable.value == null) MutableLiveData(null)
        else MutableLiveData(LivePlayable(player.playable.value!!))
    val playable: LiveData<LivePlayable?> = _playable

    private val _stream = MutableLiveData<Stream?>(player.stream.value)
    val stream: LiveData<Stream?> = _stream

    private val _state = MutableLiveData<Player.State?>(player.state.value)
    val state: LiveData<Player.State?> = _state

    private val _duration = MutableLiveData<Long?>(null)
    val duration: LiveData<Long?> = _duration

    private val _position = MutableLiveData<Long?>(null)
    val position: LiveData<Long?> = _position

    // MARK: Init

    init {
        start()
    }

    private fun start() = scope.launch {
        wirePlayable()
        wireStream()
        wireState()
        wireDuration()
        wirePosition()
    }

    // MARK: Plumbing

    private suspend fun wirePlayable() = scope.launch {
        player.playable.collect {
            _playable.value =
                if (it == null) null
                else LivePlayable(it)
        }
    }

    private suspend fun wireStream() = scope.launch {
        player.stream.collect { _stream.value = it }
    }

    private suspend fun wireState() = scope.launch {
        player.state.collect { _state.value = it }
    }

    private suspend fun wireDuration() = scope.launch {
        player.duration.collect { _duration.value = it }
    }

    private suspend fun wirePosition() = scope.launch {
        player.position.collect { _position.value = it }
    }

}

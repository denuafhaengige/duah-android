package com.denuafhaengige.duahandroid.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayableBroadcastFlow
import com.denuafhaengige.duahandroid.player.PlayableFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LivePlayableBroadcast(playableBroadcastFlow: PlayableBroadcastFlow) {

    private val _livePlayableBroadcast = MutableLiveData<Playable.Broadcast>(null)
    val livePlayableBroadcast: LiveData<Playable.Broadcast> = _livePlayableBroadcast

    init {
        MainScope().launch {
            playableBroadcastFlow.flow.collect { _livePlayableBroadcast.value = it }
        }
    }
}

class LivePlayable(playableFlow: PlayableFlow) {

    private val _livePlayable = MutableLiveData<Playable>(null)
    val livePlayable: LiveData<Playable> = _livePlayable

    init {
        MainScope().launch {
            playableFlow.flow.collect { _livePlayable.value = it }
        }
    }

}

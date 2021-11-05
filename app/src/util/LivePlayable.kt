package dk.denuafhaengige.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dk.denuafhaengige.android.player.Playable
import dk.denuafhaengige.android.player.PlayableBroadcastFlow
import dk.denuafhaengige.android.player.PlayableFlow
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

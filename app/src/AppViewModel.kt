package com.denuafhaengige.duahandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.denuafhaengige.duahandroid.content.ContentService
import com.github.michaelbull.result.*
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees
import com.denuafhaengige.duahandroid.models.Channel
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayableBroadcastFlow
import com.denuafhaengige.duahandroid.player.Player
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LiveEntity
import com.denuafhaengige.duahandroid.util.LiveFeatured
import com.denuafhaengige.duahandroid.util.LivePlayableBroadcast
import com.denuafhaengige.duahandroid.util.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.CoroutineScope.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.lang.ref.WeakReference

class AppViewModel(
    val settings: Settings,
    private val player: Player,
) : ViewModel() {

    // MARK: Types

    class Factory(
        private val settings: Settings,
        private val player: Player,
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(AppViewModel::class.java)) {
                throw IllegalArgumentException("Unknown ViewModel Class")
            }
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(settings, player) as T
        }
    }

    // MARK: Props

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var _contentService: WeakReference<ContentService>
    val contentService
        get() = _contentService.get()
    val playerViewModel = PlayerViewModel(player)

    // MARK: UI Fields

    private val _appState = MutableLiveData<AppState>(AppState.initial)
    val appState: LiveData<AppState> = _appState

    private val _featuredContent = MutableLiveData<List<LiveFeatured>>(emptyList())
    val featuredContent: LiveData<List<LiveFeatured>> = _featuredContent

    private val _latestBroadcasts = MutableLiveData<List<LivePlayableBroadcast>>(emptyList())
    val latestBroadcasts: LiveData<List<LivePlayableBroadcast>> = _latestBroadcasts

    private val _liveChannel = MutableLiveData<LiveEntity<ChannelWithCurrentBroadcast>?>(null)
    val liveChannel: LiveData<LiveEntity<ChannelWithCurrentBroadcast>?> = _liveChannel

    // Init

    init {
        start()
    }

    private fun start() = scope.launch {
        val serviceIstance = ContentService.instance.filterNotNull().first()
        _contentService = WeakReference(serviceIstance)
        wireContentService()
    }

    private suspend fun wireContentService() {
        scope.launch {
            contentService?.state?.collect {
                _appState.value = AppState.from(it)
            }
        }
        scope.launch {
            contentService?.featuredContent?.collect { featured ->
                _featuredContent.value = featured.map { LiveFeatured(it) }
            }
        }
        scope.launch {
            contentService?.latestBroadcasts?.collect { broadcasts ->
                _latestBroadcasts.value = broadcasts.mapNotNull {
                    val broadcast = it.flow.value ?: return@mapNotNull null
                    val store = contentService?.contentStore ?: return@mapNotNull null
                    LivePlayableBroadcast(
                        playableBroadcastFlow = PlayableBroadcastFlow(
                            playable = Playable.Broadcast(broadcast),
                            store = store
                        )
                    )
                }
            }
        }
        scope.launch {
            contentService?.liveChannel?.collect { channel ->
                if (channel == null) {
                    _liveChannel.value = null
                    return@collect
                }
                _liveChannel.value = LiveEntity(channel)
            }
        }
    }

    // MARK: Logic

    private suspend fun getLiveChannel(): ChannelWithCurrentBroadcast? {
        return contentService?.contentStore?.database?.channelDao()?.findByIdentifier("live")
    }

}

package com.denuafhaengige.duahandroid.content

import com.denuafhaengige.duahandroid.models.*
import com.denuafhaengige.duahandroid.player.Playable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import com.denuafhaengige.duahandroid.models.Program as ProgramEntity

class FeaturedFlow(featured: Featured, store: ContentStore) {

    private val _flow = MutableStateFlow(featured)
    private val entityFlow: EntityFlow<*>
    private val scope = CoroutineScope(Dispatchers.IO)
    val flow = _flow.asStateFlow()

    init {
        when (val flowValue = _flow.value) {
            is Featured.Broadcast -> {
                val broadcastFlow = EntityFlow(
                    entity = flowValue.entity,
                    contentStoreEventFlow = store.eventFlow,
                    fetcher = BroadcastFetcher(store),
                )
                entityFlow = broadcastFlow
                scope.launch {
                    broadcastFlow.flow
                        .filterNotNull()
                        .collect {
                            _flow.value = Featured.Broadcast(
                                entity = it,
                                playable = Playable.Broadcast(it),
                            )
                        }
                }
            }
            is Featured.Channel -> {
                val channelFlow = EntityFlow(
                    entity = flowValue.entity,
                    contentStoreEventFlow = store.eventFlow,
                    fetcher = ChannelFetcher(store),
                )
                entityFlow = channelFlow
                scope.launch {
                    channelFlow.flow
                        .filterNotNull()
                        .collect {
                            _flow.value = Featured.Channel(
                                entity = it,
                                playable = Playable.Channel(it),
                            )
                        }
                }
            }
            is Featured.Program -> {
                val programFlow = EntityFlow(
                    entity = flowValue.entity,
                    contentStoreEventFlow = store.eventFlow,
                    fetcher = ProgramFetcher(store),
                )
                entityFlow = programFlow
                scope.launch {
                    programFlow.flow
                        .filterNotNull()
                        .collect { _flow.value = Featured.Program(it) }
                }
            }
        }
    }

}

sealed class Featured: Imaged, Titled, MetaTitled, Described {
    data class Broadcast(
        val entity: BroadcastWithProgramAndEmployees,
        val playable: Playable,
        ): Featured() {
        override val wideImageUri = entity.wideImageUri
        override val squareImageUri = entity.squareImageUri
        override val title = entity.broadcast.title
        override val metaTitle = entity.metaTitle
        override val metaTitleSupplement = entity.metaTitleSupplement
        override val description = entity.description
    }
    data class Program(
        val entity: ProgramEntity,
        ): Featured() {
        override val wideImageUri = entity.wideImageUri
        override val squareImageUri = entity.squareImageUri
        override val title = entity.title
        override val metaTitle: Nothing? = null
        override val metaTitleSupplement: Nothing? = null
        override val description = entity.description
    }
    data class Channel(
        val entity: ChannelWithCurrentBroadcast,
        val playable: Playable,
        ): Featured() {
        override val wideImageUri = entity.wideImageUri
        override val squareImageUri = entity.squareImageUri
        override val title = entity.title
        override val metaTitle = entity.metaTitle
        override val metaTitleSupplement = entity.metaTitleSupplement
        override val description: Nothing? = null
    }
}

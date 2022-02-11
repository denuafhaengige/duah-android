package com.denuafhaengige.duahandroid.player

import android.net.Uri
import com.denuafhaengige.duahandroid.content.ContentStore
import com.denuafhaengige.duahandroid.content.EntityFlow
import com.denuafhaengige.duahandroid.models.*
import com.denuafhaengige.duahandroid.util.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.denuafhaengige.duahandroid.models.ChannelWithCurrentBroadcast as ChannelModel
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees as BroadcastModel

class PlayableBroadcastFlow(playable: Playable.Broadcast, store: ContentStore) {
    private val playableFlow = PlayableFlow(playable, store)
    val flow: StateFlow<Playable.Broadcast>
        @Suppress("UNCHECKED_CAST")
        get() = playableFlow.flow as StateFlow<Playable.Broadcast>
}

class PlayableFlow(playable: Playable, store: ContentStore) {

    private val _flow = MutableStateFlow(playable)
    private val entityFlow: Any
    private val scope = CoroutineScope(Dispatchers.IO)
    val flow = _flow.asStateFlow()

    init {
        when (val flowValue = _flow.value) {
            is Playable.Broadcast -> {
                val broadcastFlow = EntityFlow(
                    entity = flowValue.broadcast,
                    contentStoreEventFlow = store.eventFlow,
                    fetcher = BroadcastFetcher(store),
                )
                entityFlow = broadcastFlow
                scope.launch {
                    broadcastFlow.flow
                        .filterNotNull()
                        .collect { _flow.value = Playable.Broadcast(it) }
                }
            }
            is Playable.Channel -> {
                val channelFlow = EntityFlow(
                    entity = flowValue.channel,
                    contentStoreEventFlow = store.eventFlow,
                    fetcher = ChannelFetcher(store),
                )
                entityFlow = channelFlow
                scope.launch {
                    channelFlow.flow
                        .filterNotNull()
                        .collect { _flow.value = Playable.Channel(it) }
                }
            }
        }
    }
}

sealed class Playable: Identifiable, Titled, MetaTitled, Imaged {

    class Broadcast(val broadcast: BroadcastModel): Playable() {
        override val title: String
            get() = broadcast.title
        override val metaTitle: String?
            get() = broadcast.metaTitle
        override val metaTitleSupplement: String?
            get() = broadcast.metaTitleSupplement
        override val squareImageUri: Uri?
            get() = broadcast.squareImageUri
        override val wideImageUri: Uri?
            get() = broadcast.wideImageUri
    }
    class Channel(val channel: ChannelModel): Playable() {
        override val title: String
            get() = channel.title
        override val metaTitle: String?
            get() = channel.metaTitle
        override val metaTitleSupplement: String?
            get() = channel.metaTitleSupplement
        override val squareImageUri: Uri?
            get() = channel.squareImageUri
        override val wideImageUri: Uri?
            get() = channel.wideImageUri
    }

    // MARK: Types

    enum class EntityType(val stringValue: String) {
        BROADCAST("broadcast"),
        CHANNEL("channel"),
    }

    data class MediaItemId (
        val entityType: EntityType,
        val entityId: Int,
    ) {
        companion object {
            private const val delimiter = "___"
            fun forString(value: String): MediaItemId? {
                val components = value.split(delimiter)
                if (components.size != 2) {
                    return null
                }
                val entityTypeString = components[0]
                val entityIdString = components[1]
                val entityType = EntityType.values().find { it.stringValue == entityTypeString } ?: return null
                val entityId = entityIdString.toInt()
                return MediaItemId(entityType, entityId)
            }
        }

        val stringValue: String = "${entityType.stringValue}${delimiter}${entityId}"

        override fun equals(other: Any?): Boolean {
            if (other is MediaItemId) {
                return other.stringValue == stringValue
            }
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return stringValue.hashCode()
        }

    }

    override val id: Int
        get() = when (this) {
            is Channel -> channel.id
            is Broadcast -> broadcast.id
        }

    val mediaItemId: MediaItemId
        get() = when (this) {
            is Channel -> MediaItemId(entityType = EntityType.CHANNEL, entityId = channel.id)
            is Broadcast -> MediaItemId(entityType = EntityType.BROADCAST, entityId = broadcast.id)
        }

    fun streamsWithSettings(settings: Settings): List<Stream> = when (this) {
        is Channel -> {
            val uri = Uri.parse("${settings.radioEndpoint}/${channel.channel.identifier}")
            val liveAAC = Stream(type = StreamType.LIVE_AAC, uri = uri)
            listOf(liveAAC)
        }
        is Broadcast -> {
            val list = mutableListOf<Stream>()
            broadcast.broadcast.vodDirectFile?.let {
                val uri = Uri.parse("${settings.streamEndpoint}/direct${it.path}")
                val direct = Stream(type = StreamType.DIRECT_FILE, uri = uri)
                list.add(direct)
            }
            broadcast.broadcast.vodSegmentedFolder?.let {
                val uri = Uri.parse("${settings.streamEndpoint}/vod/hls/broadcasts/${broadcast.id}/segments/index-a1.m3u8")
                val hlsEvent = Stream(type = StreamType.HLS_EVENT, uri = uri)
                list.add(hlsEvent)
            }
            broadcast.broadcast.vodSingleFileFolder?.let {
                val uri = Uri.parse("${settings.streamEndpoint}/vod/hls/broadcasts/${broadcast.id}/single/index-a1.m3u8")
                val hlsVOD = Stream(type = StreamType.HLS_VOD, uri = uri)
                list.add(hlsVOD)
            }
            list
        }
    }

    fun preferredStreamWithSettings(settings: Settings): Stream? {
        if (this is Channel && settings.radioEndpointOverride != null) {
            return Stream(
                type = StreamType.LIVE_AAC,
                uri = settings.radioEndpointOverride!!,
            )
        }
        val streams = streamsWithSettings(settings)
        if (streams.isEmpty()) {
            return null
        }
        if (streams.size == 1) {
            return streams.first()
        }
        streams.firstOrNull { it.type == StreamType.DIRECT_FILE } ?.let { return it }
        streams.firstOrNull { it.type == StreamType.HLS_VOD } ?.let { return it }
        streams.firstOrNull { it.type == StreamType.HLS_EVENT } ?.let { return it }
        streams.firstOrNull { it.type == StreamType.LIVE_AAC } ?.let { return it }
        return null
    }

    // MARK: Static

    companion object {

        suspend fun forMediaItemId(mediaItemId: MediaItemId, contentStore: ContentStore): Playable? {
            when (mediaItemId.entityType) {
                EntityType.BROADCAST -> {
                    val broadcast = BroadcastFetcher(store = contentStore).get(mediaItemId.entityId) ?: return null
                    return Broadcast(broadcast)
                }
                EntityType.CHANNEL -> {
                    val channel = ChannelFetcher(store = contentStore).get(mediaItemId.entityId) ?: return null
                    return Channel(channel)
                }
            }
        }

        val example: Playable
            get() = Broadcast(broadcast = BroadcastModel.example)
    }
}

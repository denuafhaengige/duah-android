package com.denuafhaengige.duahandroid.content

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.content.ContentLoaderStateAdapter.Companion.asServiceState
import com.denuafhaengige.duahandroid.models.*
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.*

class ContentProvider(context: Context) {

    // MARK: Static

    companion object {
        private val mutableInstance = MutableStateFlow<ContentProvider?>(null)
        val instance = mutableInstance.asStateFlow()
    }

    // MARK: Types

    sealed class LoadingState {
        object Subscribing: LoadingState()
        data class Synchronizing(val entityType: EntityType, val number: Int, val of: Int): LoadingState()
        object Done: LoadingState()
    }

    sealed class State {
        object Initial: State()
        object WaitingForConnection: State()
        data class Loading(val state: LoadingState): State()
        object PreparingContent: State()
        object ReadyToServe: State()
    }

    // MARK: Props

    private val _state = MutableStateFlow<State>(State.Initial)
    val state = _state.asStateFlow()

    private var featuredSettingId: Int? = null
    private val _featuredContent = MutableStateFlow<List<FeaturedFlow>>(emptyList())
    val featuredContent = _featuredContent.asStateFlow()

    private val _latestBroadcasts = MutableStateFlow<List<EntityFlow<BroadcastWithProgramAndEmployees>>>(emptyList())
    val latestBroadcasts = _latestBroadcasts.asStateFlow()

    private val _liveChannel = MutableStateFlow<EntityFlow<ChannelWithCurrentBroadcast>?>(null)
    val liveChannel = _liveChannel.asStateFlow()

    private val _programs = MutableStateFlow<List<EntityFlow<Program>>>(emptyList())
    val programs = _programs.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private val contentLoader: ContentLoader
    val contentStore: ContentStore
    private val settings: Settings
        get() = Application.settings
    private val okHttpClient: OkHttpClient
        get() = Application.okHttpClient
    private val moshi: Moshi
        get() = Application.moshi

    // MARK: Service

    init {
        Timber.d("ContentProvider | onCreate")
        mutableInstance.value = this
        contentStore = ContentStore(context = context)
        contentLoader = ContentLoader(
            store = contentStore,
            settings = settings,
            okHttpClient = okHttpClient,
            moshi = moshi
        )
        scope.launch { start() }
    }

    // MARK: Wiring

    @OptIn(FlowPreview::class)
    private suspend fun wireContentStore() {
        scope.launch {
            contentStore.state.collect { contentStoreState ->
                Timber.d("ContentProvider | contentStore.state.collect | state: $contentStoreState")
                when (contentStoreState) {
                    ContentStore.State.READY -> contentStoreReady()
                    else -> return@collect
                }
            }
        }
        scope.launch {
            contentStore.eventFlow
                .filterIsInstance<ContentStore.Event.Loaded>()
                .collect { event ->
                    Timber.d("ContentProvider | state: ${state.value} | contentStore.eventFlow.collect | event: $event")
                }
        }
        scope.launch {
            contentStore.eventFlow
                .filterIsInstance<ContentStore.Event.Loaded>()
                .flatMapMerge { it.operations.asFlow() }
                .filterIsInstance<ContentStore.Operation.Upsert>()
                .filter { it.id == featuredSettingId }
                .collect {
                    if (state.value !is State.ReadyToServe) {
                        return@collect
                    }
                    refreshFeaturedContent()
                }
        }
        scope.launch {
            contentStore.eventFlow
                .filterIsInstance<ContentStore.Event.Loaded>()
                .flatMapMerge { it.operations.asFlow() }
                .filter { it.entityType == EntityType.BROADCAST }
                .collect {
                    if (state.value !is State.ReadyToServe) {
                        return@collect
                    }
                    val latestBroadcastIds = latestBroadcasts.value.mapNotNull { value -> value.flow.value?.id }
                    when (it) {
                        is ContentStore.Operation.Delete -> {
                            if (it.id in latestBroadcastIds) {
                                refreshLatestBroadcasts()
                            }
                        }
                        is ContentStore.Operation.Upsert -> {
                            refreshLatestBroadcasts()
                        }
                    }
                }
        }
        scope.launch {
            contentStore.eventFlow
                .filterIsInstance<ContentStore.Event.Loaded>()
                .flatMapMerge { it.operations.asFlow() }
                .filter { it.entityType == EntityType.PROGRAM }
                .collect {
                    if (state.value !is State.ReadyToServe) {
                        return@collect
                    }
                    refreshPrograms()
                }
        }
    }

    private suspend fun wireContentLoader() {
        scope.launch {
            contentLoader.state.collect { contentLoaderState ->
                Timber.d("ContentProvider | contentLoader.state.collect | state: $contentLoaderState")
                if (_state.value in listOf(State.PreparingContent, State.ReadyToServe)) {
                    return@collect
                }
                _state.value = contentLoaderState.asServiceState()
            }
        }
    }

    private suspend fun wireSelf() {
        scope.launch {
            state.collect {
                if (it !is State.PreparingContent) {
                    return@collect
                }
                refreshFeaturedContent()
                refreshLatestBroadcasts()
                refreshLiveChannel()
                refreshPrograms()
                _state.value = State.ReadyToServe
            }
        }
    }

    // MARK: Implementation

    private suspend fun start() {
        wireContentStore()
        wireContentLoader()
        wireSelf()
        if (shouldResetDatabase()) {
            resetDatabase()
        }
        try {
            contentStore.start()
        } catch (e: Throwable) {
            Timber.d("ContentProvider | start | failed starting database with exception: $e")
            Timber.d("ContentProvider | start | resetting, then retrying")
            resetDatabase()
            contentStore.start()
        }
    }

    private suspend fun hasPerformedFullLoad(): Boolean {
        if (settings.lastFullContentLoadVersion() == null) {
            return false
        }
        return true
    }

    private suspend fun shouldResetDatabase(): Boolean {
        val lastFullContentLoadVersion = settings.lastFullContentLoadVersion() ?: return false
        return lastFullContentLoadVersion.isLowerThan(settings.dataVersionResetThreshold)
    }

    private suspend fun resetDatabase() {
        settings.setLastFullContentLoadVersion(null)
        settings.setNewestChangeDetectedDuringLoad(null)
        contentStore.resetDatabase()
    }

    private suspend fun contentStoreReady() {
        if (hasPerformedFullLoad()) {
            _state.value = State.PreparingContent
        }
        contentLoader.start()
    }

    private suspend fun refreshFeaturedContent() {
        val featuredSetting = contentStore.database.settingDao().findByIdentifier(FeaturedSetting.Identifier).firstOrNull() ?: return
        featuredSettingId = featuredSetting.id
        val itemsString = featuredSetting.value
        val itemsType = Types.newParameterizedType(List::class.java, FeaturedSetting.Value::class.java)
        val adapter: JsonAdapter<List<FeaturedSetting.Value>> = moshi.adapter(itemsType)
        val items = adapter.fromJson(itemsString) ?: return
        val broadcastIds = items
            .filter { it.type == FeaturedSetting.ValueType.BROADCAST }
            .map { it.id }
            .toIntArray()
        val broadcasts =
            if (broadcastIds.isNotEmpty()) contentStore.database.broadcastDao().loadAllByIds(broadcastIds)
            else emptyList()
        val programIds = items
            .filter { it.type == FeaturedSetting.ValueType.PROGRAM }
            .map { it.id }
            .toIntArray()
        val programs =
            if (programIds.isNotEmpty()) contentStore.database.programDao().loadAllByIds(programIds)
            else emptyList()
        val channelsIds = items
            .filter { it.type == FeaturedSetting.ValueType.CHANNEL }
            .map { it.id }
            .toIntArray()
        val channels =
            if (channelsIds.isNotEmpty()) contentStore.database.channelDao().loadAllByIds(channelsIds)
            else emptyList()
        val featuredItems = items.mapNotNull { item ->
            when (item.type) {
                FeaturedSetting.ValueType.BROADCAST -> {
                    val broadcast = broadcasts
                        .firstOrNull { it.broadcast.id == item.id } ?: return@mapNotNull null
                    FeaturedFlow(
                        featured = Featured.Broadcast(
                            entity = broadcast,
                            playable = Playable.Broadcast(broadcast),
                        ),
                        store = contentStore,
                    )
                }
                FeaturedSetting.ValueType.PROGRAM -> {
                    val program = programs
                        .firstOrNull { it.id == item.id } ?: return@mapNotNull null
                    FeaturedFlow(
                        featured = Featured.Program(entity = program),
                        store = contentStore,
                    )
                }
                FeaturedSetting.ValueType.CHANNEL -> {
                    val channel = channels
                        .firstOrNull { it.channel.id == item.id } ?: return@mapNotNull null
                    FeaturedFlow(
                        featured = Featured.Channel(
                            entity = channel,
                            playable = Playable.Channel(channel),
                        ),
                        store = contentStore,
                    )
                }
            }
        }
        _featuredContent.value = featuredItems
    }

    private suspend fun refreshLatestBroadcasts() {
        val latestBroadcasts = contentStore.database.broadcastDao().getRecentNonHiddenWithProgram(limit = 10)
        _latestBroadcasts.value = latestBroadcasts.map {
            EntityFlow(
                entity = it,
                contentStoreEventFlow = contentStore.eventFlow,
                fetcher = BroadcastFetcher(store = contentStore),
            )
        }
    }

    private suspend fun refreshLiveChannel() {
        val liveChannel = contentStore.database.channelDao().findByIdentifier("live")
        if (liveChannel == null) {
            _liveChannel.value = null
            return
        }
        _liveChannel.value = EntityFlow(
            entity = liveChannel,
            fetcher = ChannelFetcher(store = contentStore),
            contentStoreEventFlow = contentStore.eventFlow,
        )
    }

    private suspend fun refreshPrograms() {
        val allPrograms = contentStore.database.programDao().getAll()
        val allNonHiddenPrograms = allPrograms.filter { !it.hidden }
        val hardcodedSort = listOf(
            "enuafhaengigmorgen",
            "ringhvisjegtagerfejl",
            "denuundgaaelige",
            "underudvikling",
            "stramdiskurs2",
            "classic",
            "enuafhaengigsommer",
            "andersstjernholmpodcast",
        )
        val sorted = allNonHiddenPrograms.sortedWith(Comparator { lhs, rhs ->
                val lhsIndex = lhs.identifier?.let { identifier ->
                    hardcodedSort.indexOf(identifier).takeUnless { it == -1 }
                }
                val rhsIndex = rhs.identifier?.let { identifier ->
                    hardcodedSort.indexOf(identifier).takeUnless { it == -1 }
                }
                when {
                    lhsIndex == null && rhsIndex == null ->
                        return@Comparator 0
                    lhsIndex != null && rhsIndex == null ->
                        return@Comparator -1
                    lhsIndex == null && rhsIndex != null ->
                        return@Comparator 1
                    lhsIndex != null && rhsIndex != null ->
                        return@Comparator lhsIndex - rhsIndex
                    else -> throw Throwable("Shut up")
                }
            }
        )
        _programs.value = sorted.map {
            EntityFlow(
                entity = it,
                fetcher = ProgramFetcher(store = contentStore),
                contentStoreEventFlow = contentStore.eventFlow,
            )
        }
    }

}

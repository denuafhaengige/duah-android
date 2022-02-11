package com.denuafhaengige.duahandroid.content

import android.net.Uri
import com.denuafhaengige.duahandroid.graph.*
import com.denuafhaengige.duahandroid.models.*
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import com.google.common.reflect.TypeToken
import com.squareup.moshi.Moshi
import com.tinder.scarlet.*
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import java.util.*

class ContentLoader(val store: ContentStore, val settings: Settings, val moshi: Moshi) {

    // MARK: Types

    sealed class SyncState {
        interface EntityTyped {
            val entityType: EntityType
        }
        object Initial: SyncState()
        class Send(
            override val entityType: EntityType,
            val message: GraphRequestMessage): SyncState(), EntityTyped
        class AwaitResponse(
            override val entityType: EntityType,
            val message: GraphRequestMessage): SyncState(), EntityTyped
        class HandleResponse(
            override val entityType: EntityType,
            val response: GraphRequestResponseMessage): SyncState(), EntityTyped
        object Done: SyncState()
    }

    sealed class State {
        object Paused: State()
        object Starting: State()
        object WaitingForConnection: State()
        object Connected: State()
        class Subscribing(val messageId: String): State()
        object Subscribed: State()
        class Synchronizing(val syncState: SyncState):State()
        object Loaded: State()
    }

    companion object {
        val entityTypeSyncSequence = listOf(
            EntityType.EMPLOYEE,
            EntityType.CHANNEL,
            EntityType.PROGRAM,
            EntityType.BROADCAST,
            EntityType.SETTING,
        )
    }

    // MARK: Properties

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Paused)
    var state = _state.asStateFlow()

    private lateinit var graphService: GraphService
    private val lifecycle = LifecycleRegistry()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val disposables: MutableList<Disposable> = mutableListOf()
    private var requestSince: Date? = null
    private var newestChangeDetectedDuringLoad: Date? = null

    // MARK: Init

    init {
        scope.launch { state.collect { runLoop() } }
    }

    // MARK: Interface

    fun start() {
        if (state.value !is State.Paused) {
            return
        }
        _state.value = State.Starting
    }

    fun stop() {
        lifecycle.onNext(Lifecycle.State.Destroyed)
        for (disposable in disposables) {
            disposable.dispose()
        }
        disposables.removeAll { true }
        _state.value = State.Paused
    }

    // MARK: State Machine

    private suspend fun runLoop() {
        when (state.value) {
            is State.Starting -> onStarting()
            is State.Connected -> onConnected()
            is State.Subscribed -> onSubscribed()
            is State.Synchronizing -> onSynchronizing()
            else -> return
        }
    }

    private fun onStarting() {
        Log.debug("ContentLoader | onStarting()")
        if (!this::graphService.isInitialized) {
            Log.debug("ContentLoader | onStarting() | initializing GraphService")
            graphService = GraphServiceFactory(moshi = moshi).createService(settings.graphEndpoint, lifecycle)
            wireGraphService()
        }
        Log.debug("ContentLoader | onStarting() | starting GraphService")
        _state.value = State.WaitingForConnection
        lifecycle.onNext(Lifecycle.State.Started)
    }

    private fun onConnected() {
        if (state.value !is State.Connected) {
            return
        }
        val message = GraphSubscriptionMessage(
            id = UUID.randomUUID().toString(),
            subscriptionType = GraphSubscriptionType.ENTITY_UPDATES,
        )
        _state.value = State.Subscribing(messageId = message.id)
        graphService.sendSubscription(message)
    }

    private fun onSubscribed() {
        if (state.value !is State.Subscribed) {
            return
        }
        _state.value = State.Synchronizing(SyncState.Initial)
    }

    private suspend fun onSynchronizing() {
        if (state.value !is State.Synchronizing) {
            return
        }
        when ((state.value as? State.Synchronizing)?.syncState) {
            is SyncState.Initial -> onSyncInitial()
            is SyncState.Send -> onSyncSend()
            is SyncState.HandleResponse -> onSyncHandleResponse()
            is SyncState.Done -> onSyncDone()
            else -> return
        }
    }

    // MARK: Sync State Machine

    private suspend fun onSyncInitial() {
        val newestChangeFromStore = settings.newestChangeDetectedDuringLoad()
        newestChangeDetectedDuringLoad = newestChangeFromStore
        requestSince = newestChangeFromStore
        syncNextEntityType(pastType = null)
    }

    private fun onSyncSend() {
        val syncState = (state.value as? State.Synchronizing)?.syncState ?: return
        val entityType = (syncState as? SyncState.Send)?.entityType ?: return
        val message = (syncState as? SyncState.Send)?.message ?: return
        _state.value = State.Synchronizing(SyncState.AwaitResponse(entityType = entityType, message = message))
        graphService.sendRequest(message)
    }

    private suspend fun onSyncHandleResponse() {
        val syncState = (state.value as? State.Synchronizing)?.syncState ?: return
        val entityType = (syncState as? SyncState.HandleResponse)?.entityType ?: return
        val response = (syncState as? SyncState.HandleResponse)?.response ?: return
        val loadable = loadableForGraphResponse(response)
        if (loadable != null && loadable.isNotEmpty()) {
            storeEntities(entityType = entityType, loadable = loadable)
        }
        deriveNewestChange(response)
        if (response.result?.pageInfo?.hasNextPage == true) {
            val lastCursor = response.result.edges.last().cursor
            val message = requestMessage(entityType = entityType, cursor = lastCursor)
            _state.value = State.Synchronizing(SyncState.Send(entityType = entityType, message = message))
            return
        }
        syncNextEntityType(pastType = entityType)
    }

    private suspend fun onSyncDone() {
        if (settings.lastFullContentLoadVersion() == null) {
            settings.setLastFullContentLoadVersion(settings.appVersion)
        }
        storeNewestChangeDetected()
        _state.value = State.Loaded
    }

    private suspend fun storeNewestChangeDetected() {
        val newestChangeDetected = newestChangeDetectedDuringLoad ?: return
        val newestChangeFromStore = settings.newestChangeDetectedDuringLoad()
        if (newestChangeFromStore != null && newestChangeDetected < newestChangeFromStore) {
            return
        }
        settings.setNewestChangeDetectedDuringLoad(newestChangeDetected)
    }

    private fun deriveNewestChange(response: GraphRequestResponseMessage) {
        val edges = response.result?.edges ?: return
        val derived = edges.fold(newestChangeDetectedDuringLoad) { acc, edge ->
            val edgeUpdatedAt = edge.metaData.updatedAt
            if (acc != null && acc > edgeUpdatedAt) {
                return@fold acc
            }
            return@fold edgeUpdatedAt
        }
        newestChangeDetectedDuringLoad = derived
    }

    private fun syncNextEntityType(pastType: EntityType?) {
        val nextEntityType = nextEntityTypeForSync(pastEntityType = pastType)
        if (nextEntityType == null) {
            _state.value = State.Synchronizing(SyncState.Done)
            return
        }
        val message = requestMessage(entityType = nextEntityType)
        _state.value = State.Synchronizing(SyncState.Send(entityType = nextEntityType, message = message))
    }

    private fun nextEntityTypeForSync(pastEntityType: EntityType?): EntityType? {
        if (pastEntityType == null) {
            return entityTypeSyncSequence[0]
        }
        if (pastEntityType == entityTypeSyncSequence.last()) {
            return null
        }
        val pastIndex = entityTypeSyncSequence.indexOf(pastEntityType)
        return entityTypeSyncSequence[pastIndex + 1]
    }

    private fun loadableForGraphResponse(response: GraphRequestResponseMessage): List<ContentStore.Loadable<*>>? {
        return response.result?.edges?.map {
            ContentStore.Loadable(metaData = it.metaData, entity = it.node)
        }
    }

    @Suppress("UNCHECKED_CAST", "UnstableApiUsage")
    private suspend fun storeEntities(entityType: EntityType, loadable: List<ContentStore.Loadable<*>>) {
        when (entityType) {
            EntityType.BROADCAST ->
                store.load(subjects = loadable as List<ContentStore.Loadable<Broadcast>>, TypeToken.of(Broadcast::class.java))
            EntityType.PROGRAM ->
                store.load(subjects = loadable as List<ContentStore.Loadable<Program>>, TypeToken.of(Program::class.java))
            EntityType.CHANNEL ->
                store.load(subjects = loadable as List<ContentStore.Loadable<Channel>>, TypeToken.of(Channel::class.java))
            EntityType.EMPLOYEE ->
                store.load(subjects = loadable as List<ContentStore.Loadable<Employee>>, TypeToken.of(Employee::class.java))
            EntityType.SETTING ->
                store.load(subjects = loadable as List<ContentStore.Loadable<Setting>>, TypeToken.of(Setting::class.java))
            else -> return
        }
    }

    private fun requestMessage(
        entityType: EntityType,
        cursor: String? = null
    ): GraphRequestMessage {
        val connectionRequest = connectionRequest(entityType = entityType, cursor = cursor)
        return GraphRequestMessage(
            source = connectionRequest.source(),
            variables = connectionRequest.variables()
        )
    }

    private fun connectionRequest(entityType: EntityType, cursor: String? = null): GraphConnectionRequest {
        val queryArgs = queryArgs()
        val pagination = Pagination.Forward(first = 100, after = cursor)
        return when (entityType) {
            EntityType.EMPLOYEE -> GraphEmployeeConnectionRequest(queryArgs, pagination)
            EntityType.BROADCAST -> GraphBroadcastConnectionRequest(queryArgs, pagination)
            EntityType.CHANNEL -> GraphChannelConnectionRequest(queryArgs, pagination)
            EntityType.PROGRAM -> GraphProgramsConnectionRequest(queryArgs, pagination)
            EntityType.SETTING -> GraphSettingsConnectionRequest(queryArgs, pagination)
            else -> throw Throwable("Entity type not supported: $entityType")
        }
    }

    private fun queryArgs(): QueryArgs? {
        val requestSince = requestSince ?: return QueryArgs(metaDataQuery = mapOf(Pair("deletedAt", null)), nodeQuery = null)
        return QueryArgs(metaDataQuery = mapOf(Pair("updatedAt_gt", requestSince)), nodeQuery = null)
    }

    // MARK: Graph Service

    private fun wireGraphService() {
        disposables.add(graphService.observeWebSocketEvent().subscribe { event ->
            Log.debug("ContentLoader | observeWebSocketEvent | event: $event")
            if (event is WebSocket.Event.OnConnectionOpened<*>) {
                _state.value = State.Connected
            }
            if (event is WebSocket.Event.OnConnectionFailed) {
                _state.value = State.WaitingForConnection
            }
        })
        disposables.add(graphService.receiveSubscriptionResponse().subscribe { subscriptionResponseMessage ->
            Log.debug("ContentLoader | receiveSubscriptionResponse | subscriptionResponseMessage: $subscriptionResponseMessage")
            if ((state.value as? State.Subscribing)?.messageId == subscriptionResponseMessage.respondingToMessageId) {
                _state.value = State.Subscribed
            }
        })
        disposables.add(graphService.receiveSubscriptionUpdate().subscribe { message ->
            Log.debug("ContentLoader | receiveSubscriptionUpdate | subscriptionUpdateMessage: $message")
            scope.launch {
                storeEntities(
                    entityType = message.payload.entityType,
                    loadable = listOf(ContentStore.Loadable(
                        metaData = message.payload.metaData, entity = message.payload.entity
                    ))
                )
            }
        })
        disposables.add(graphService.receiveRequestResponse().subscribe { requestResponseMessage ->
            Log.debug("ContentLoader | receiveRequestResponse | requestResponseMessage: ${requestResponseMessage.id}")
            val syncState = (state.value as? State.Synchronizing)?.syncState ?: return@subscribe
            val entityType = (syncState as? SyncState.AwaitResponse)?.entityType ?: return@subscribe
            val message = (syncState as? SyncState.AwaitResponse)?.message ?: return@subscribe
            if (message.id == requestResponseMessage.respondingToMessageId) {
                _state.value = State.Synchronizing(SyncState.HandleResponse(
                    entityType = entityType,
                    response = requestResponseMessage
                ))
            }
        })
    }
}
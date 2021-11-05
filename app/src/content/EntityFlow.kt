package dk.denuafhaengige.android.content

import dk.denuafhaengige.android.models.Entity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class EntityFlow<T: Entity>(
    entity: T,
    fetcher: Fetcher<T>,
    contentStoreEventFlow: Flow<ContentStore.Event>,
) {

    interface Fetcher<T> {
        suspend fun get(id: Int): T?
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _flow = MutableStateFlow<T?>(entity)
    val flow = _flow.asStateFlow()

    init {
        val entityId = entity.id
        val entityType = entity.entityType
        scope.launch {
            contentStoreEventFlow
                .filterIsInstance<ContentStore.Event.Loaded>()
                .flatMapMerge { it.operations.asFlow() }
                .filter { it.id == entity.id && it.entityType == entityType }
                .collect {
                    _flow.value = fetcher.get(entityId)
                }
        }
    }

}
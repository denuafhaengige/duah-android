package com.denuafhaengige.duahandroid.content

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.denuafhaengige.duahandroid.graph.GraphConnectionEdge
import com.denuafhaengige.duahandroid.models.*
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ContentStore(val context: Context) {

    enum class State {
        IDLE,
        READY,
    }

    data class Loadable<T: Entity>(
        val metaData: EntityMetaData,
        val entity: T
    )

    sealed class Operation(open val id: Int, open val entityType: EntityType) {
        data class Upsert(override val id: Int, override val entityType: EntityType): Operation(id, entityType)
        data class Delete(override val id: Int, override val entityType: EntityType): Operation(id, entityType)
    }

    sealed class Event {
        data class Loaded(val operations: List<Operation>): Event()
    }

    private companion object {
        const val databaseName = "duah-database"
    }

    // MARK: Props

    private val scope = CoroutineScope(Dispatchers.IO)
    lateinit var database: Database

    private val _state = MutableStateFlow<State>(State.IDLE)
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    // MARK: Public Interface

    fun resetDatabase() = scope.launch {
        Timber.d("ContentLoader | resetDatabase")
        context.deleteDatabase(databaseName)
    }

    fun start() = scope.launch {
        database = Room.databaseBuilder(context, Database::class.java, databaseName).build()
        _state.value = State.READY
    }

    @Suppress("UnstableApiUsage")
    suspend fun <T: Entity>load(subjects: List<Loadable<T>>, type: TypeToken<T>) = scope.launch {
        val upsertEntities = subjects
            .filter { it.metaData.deletedAt == null }
            .map { it.entity }
        upsert(entities = upsertEntities, type = type)
        val upsertOperations = upsertEntities
            .map { Operation.Upsert(id = it.id, entityType = EntityType.by(type)) }
        wireRelations(entities = upsertEntities, type = type)
        val deleteEntities = subjects
            .filter { it.metaData.deletedAt != null }
            .map { it.entity }
        delete(entities = deleteEntities, type = type)
        val deleteOperations = deleteEntities
            .map { Operation.Delete(id = it.id, entityType = EntityType.by(type)) }
        val operations = upsertOperations + deleteOperations
        _eventFlow.emit(Event.Loaded(operations))
    }

    // MARK: Implementation

    @Suppress("UNCHECKED_CAST", "UnstableApiUsage")
    private suspend fun <T: Entity>wireRelations(entities: List<T>, type: TypeToken<T>) {
        when (type.rawType) {
            Broadcast::class.java -> wireBroadcastRelations(entities = entities as List<Broadcast>)
        }
    }

    private suspend fun wireBroadcastRelations(entities: List<Broadcast>) {
        val deleteRelations = entities
            .filter { it.hostEmployeeReferences.isEmpty() }
            .map { BroadcastId(it.id) }
        val upsertRelations = entities
            .flatMap { broadcast ->
                val broadcastId = broadcast.id
                broadcast.hostEmployeeReferences.map { employeeRef ->
                    BroadcastEmployeeCrossRef(
                        broadcastReference = EntityReference(broadcastId),
                        employeeReference = employeeRef
                    )
                }
            }
        database.relationsDao().deleteBroadcastEmployeeCrossRefs(deleteRelations)
        database.relationsDao().insertBroadcastEmployeeCrossRefs(upsertRelations)
    }

    @Suppress("UNCHECKED_CAST", "UnstableApiUsage")
    private suspend fun <T: Entity>upsert(entities: List<T>, type: TypeToken<T>) {
        when (type.rawType) {
            Broadcast::class.java -> database.broadcastDao().upsertAll(entities as List<Broadcast>)
            Employee::class.java -> database.employeeDao().upsertAll(entities as List<Employee>)
            Channel::class.java -> database.channelDao().upsertAll(entities as List<Channel>)
            Program::class.java -> database.programDao().upsertAll(entities as List<Program>)
            Setting::class.java -> database.settingDao().upsertAll(entities as List<Setting>)
        }
    }

    @Suppress("UNCHECKED_CAST", "UnstableApiUsage")
    private suspend fun <T: Entity>delete(entities: List<T>, type: TypeToken<T>) {
        when (type.rawType) {
            Broadcast::class.java -> database.broadcastDao().deleteAll(entities as List<Broadcast>)
            Employee::class.java -> database.employeeDao().deleteAll(entities as List<Employee>)
            Channel::class.java -> database.channelDao().deleteAll(entities as List<Channel>)
            Program::class.java -> database.programDao().deleteAll(entities as List<Program>)
            Setting::class.java -> database.settingDao().deleteAll(entities as List<Setting>)
        }
    }

}

@androidx.room.Database(entities = [Broadcast::class, Channel::class, Employee::class, Program::class, Setting::class, BroadcastEmployeeCrossRef::class], version = 1)
@TypeConverters(value = [RoomConverters::class, EmployeeTypeAdapter::class, FileTypeAdapter::class, SettingIdentifierAdapter::class])
abstract class Database : RoomDatabase() {
    abstract fun broadcastDao(): BroadcastDao
    abstract fun channelDao(): ChannelDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun programDao(): ProgramDao
    abstract fun settingDao(): SettingDao
    abstract fun relationsDao(): RelationsDao
}

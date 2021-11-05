package com.denuafhaengige.duahandroid.models

import com.google.common.reflect.TypeToken
import com.squareup.moshi.*
import java.lang.reflect.Type
import java.util.*

// MARK: Annotations

@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.EXPRESSION
)
@Retention(AnnotationRetention.SOURCE)
@JsonQualifier
annotation class JsonEntityType

// MARK: Enums

enum class EntityType(val stringValue: String) {

    BROADCAST("broadcast"),
    CHANNEL("channel"),
    ELEMENT("element"),
    FILE("file"),
    INTEREST_SUBSCRIBER("interest_subscriber"),
    PAGE("page"),
    PAYMENT("payment"),
    PROGRAM("program"),
    SETTING("setting"),
    SUBSCRIPTION("subscription"),
    EMPLOYEE("employee"),
    UNKNOWN("");

    companion object {
        fun <T: Entity>by(type: TypeToken<T>): EntityType {
            return when (type.rawType) {
                Broadcast::class.java -> BROADCAST
                Program::class.java -> PROGRAM
                Employee::class.java -> EMPLOYEE
                Setting::class.java -> SETTING
                Channel::class.java -> CHANNEL
                else -> UNKNOWN
            }
        }
    }

    fun type(): Type {
        return when (this) {
            BROADCAST -> Broadcast::class.java
            EMPLOYEE -> Employee::class.java
            PROGRAM -> Program::class.java
            FILE -> File::class.java
            SETTING -> Setting::class.java
            else -> Object::class.java
        }
    }
}

// MARK: Adapters

class EntityTypeAdapter: JsonAdapter<EntityType>() {

    @ToJson
    override fun toJson(@JsonEntityType writer: JsonWriter, value: EntityType?) {
        writer.value(value?.stringValue)
    }

    @FromJson
    @JsonEntityType
    override fun fromJson(reader: JsonReader): EntityType? {
        val string = reader.nextString()
        return EntityType.values().firstOrNull { candidate -> candidate.stringValue == string }
    }
}

// MARK: Data Types

data class EntityMetaData(
    val createdAt: Date,
    val updatedAt: Date,
    val deletedAt: Date?,
)

data class EntityReference<T: Entity>(
    val id: Int
)

interface Entity: Identifiable {

    val entityType: EntityType
        get() {
            return when (this) {
                is Broadcast,
                is BroadcastWithProgramAndEmployees -> EntityType.BROADCAST
                is Employee -> EntityType.EMPLOYEE
                is Program -> EntityType.PROGRAM
                is Setting -> EntityType.SETTING
                is Channel,
                is ChannelWithCurrentBroadcast -> EntityType.CHANNEL
                else -> throw Throwable("Unhandled entity class: $this")
            }
        }
}

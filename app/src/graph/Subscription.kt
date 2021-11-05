package com.denuafhaengige.duahandroid.graph

import com.denuafhaengige.duahandroid.models.Entity
import com.denuafhaengige.duahandroid.models.EntityMetaData
import com.denuafhaengige.duahandroid.models.EntityType
import com.denuafhaengige.duahandroid.models.JsonEntityType
import com.squareup.moshi.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

// MARK: Annotations

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonGraphSubscriptionType

// MARK: Enums

enum class GraphSubscriptionType(val stringValue: String) {
    ENTITY_UPDATES("entity_updates")
}

// MARK: Adapters

class GraphSubscriptionUpdateMessagePayloadAdapterFactory: JsonAdapter.Factory {

    class GraphSubscriptionUpdateMessagePayloadAdapter(
        val moshi: Moshi,
        val factory: JsonAdapter.Factory,
    ): JsonAdapter<GraphSubscriptionUpdateMessagePayload<*>>() {

        override fun fromJson(reader: JsonReader): GraphSubscriptionUpdateMessagePayload<*>? {
            val readerCopy = reader.peekJson()
            val entityTypeString = entityTypeStringFromJson(reader = readerCopy) ?: return null
            val entityType =
                EntityType.values().find { candidate -> candidate.stringValue == entityTypeString }
                    ?: return null
            val adapter = adapter(entityType.type())
            return adapter.fromJson(reader)
        }

        private fun entityTypeStringFromJson(reader: JsonReader): String? {
            val jsonType = reader.peek()
            if (jsonType != JsonReader.Token.BEGIN_OBJECT) {
                return null
            }
            val option = JsonReader.Options.of("entityType")
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.selectName(option)) {
                    0 -> return reader.nextString()
                    else -> {
                        reader.skipName()
                        reader.skipValue()
                    }
                }
            }
            return null
        }

        private fun adapter(type: Type): JsonAdapter<GraphSubscriptionUpdateMessagePayload<*>> {
            val payloadType = Types.newParameterizedType(GraphSubscriptionUpdateMessagePayload::class.java, type)
            return moshi.nextAdapter(factory, payloadType, emptySet())
        }

        override fun toJson(writer: JsonWriter, value: GraphSubscriptionUpdateMessagePayload<*>?) {
            throw Throwable("toJson not supported")
        }
    }

    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi
    ): JsonAdapter<GraphSubscriptionUpdateMessagePayload<*>>? {
        if (annotations.isNotEmpty()) {
            return null
        }
        if (type !is ParameterizedType) {
            return null
        }
        if (type.rawType != GraphSubscriptionUpdateMessagePayload::class.java) {
            return null
        }
        return GraphSubscriptionUpdateMessagePayloadAdapter(moshi = moshi, factory = this)
    }
}

class GraphSubscriptionTypeAdapter {
    @ToJson
    fun toJson(@JsonGraphSubscriptionType value: GraphSubscriptionType): String {
        return value.stringValue
    }
    @FromJson
    @JsonGraphSubscriptionType
    fun fromJson(value: String): GraphSubscriptionType? {
        return GraphSubscriptionType.values().firstOrNull { candidate -> candidate.stringValue == value }
    }
}

// MARK: Data Types

data class GraphSubscriptionMessage(
    override var id: String,
    @JsonGraphSubscriptionType
    val subscriptionType: GraphSubscriptionType,
): GraphMessage(
    id = id,
    subtype = GraphMessageType.SUBSCRIPTION,
)

data class GraphSubscriptionResponseMessage(
    override var id: String,
    override var respondingToMessageId: String?,
    val result: Boolean,
    val error: String?,
): GraphMessage(
    id = id,
    respondingToMessageId = respondingToMessageId,
    subtype = GraphMessageType.SUBSCRIPTION_RESPONSE,
)

data class GraphSubscriptionUpdateMessage(
    override var id: String,
    @JsonGraphSubscriptionType
    val subscriptionType: GraphSubscriptionType,
    val payload: GraphSubscriptionUpdateMessagePayload<*>,
): GraphMessage(
    id = id,
    subtype = GraphMessageType.SUBSCRIPTION_UPDATE,
)

data class GraphSubscriptionUpdateMessagePayload<T: Entity>(
    @JsonEntityType
    val entityType: EntityType,
    val metaData: EntityMetaData,
    val entity: T,
)

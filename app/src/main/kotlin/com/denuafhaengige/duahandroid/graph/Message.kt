package com.denuafhaengige.duahandroid.graph

import com.google.common.graph.Graph
import com.squareup.moshi.*

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.denuafhaengige.duahandroid.models.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

// MARK: Annotations

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonMessageType

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonGraphMessageType

// MARK: Enums

enum class MessageType(val stringValue: String) {
    GRAPH("graph")
}

enum class GraphMessageType(val stringValue: String) {
    REQUEST("request"),
    REQUEST_RESPONSE("request_response"),
    SUBSCRIPTION("subscription"),
    SUBSCRIPTION_RESPONSE("subscription_response"),
    SUBSCRIPTION_UPDATE("subscription_update"),
    COMMAND("command"),
    COMMAND_RESPONSE("command_response"),
}

// MARK: Adapters

class GraphMessageTypeAdapter {

    @ToJson
    fun toJson(@JsonGraphMessageType value: GraphMessageType): String {
        return value.stringValue
    }

    @FromJson
    @JsonGraphMessageType
    fun fromJson(value: String): GraphMessageType? {
        return GraphMessageType.values().firstOrNull { candidate -> candidate.stringValue == value }
    }
}

class MessageTypeAdapter {

    @ToJson
    fun toJson(@JsonMessageType value: MessageType): String {
        return value.stringValue
    }

    @FromJson
    @JsonMessageType
    fun fromJson(value: String): MessageType? {
        return MessageType.values().firstOrNull { candidate -> candidate.stringValue == value }
    }
}

class GraphMessageJsonAdapterFactory: JsonAdapter.Factory {

    class GraphMessageJsonAdapter(val moshi: Moshi, val factory: Factory, val targetType: Type): JsonAdapter<GraphMessage>() {

        override fun fromJson(reader: JsonReader): GraphMessage? {
            val readerCopy = reader.peekJson()
            val options = JsonReader.Options.of("type", "subtype")
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
                throw Throwable("Expected BEGIN_OBJECT")
            }
            reader.beginObject()
            var type: MessageType? = null
            var subType: GraphMessageType? = null
            while (reader.hasNext()) {
                when (reader.selectName(options)) {
                    0 -> {
                        val typeString = reader.nextString()
                        type = MessageType.values().firstOrNull { it.stringValue == typeString }
                    }
                    1 -> {
                        val subTypeString = reader.nextString()
                        subType = GraphMessageType.values().firstOrNull { it.stringValue == subTypeString }
                    }
                    else -> {
                        reader.skipName()
                        reader.skipValue()
                    }
                }
            }
            reader.endObject()
            if (type != MessageType.GRAPH || subType == null) {
                return null
            }
            val messageClass = when (subType) {
                GraphMessageType.REQUEST -> GraphRequestMessage::class.java
                GraphMessageType.REQUEST_RESPONSE -> GraphRequestResponseMessage::class.java
                GraphMessageType.SUBSCRIPTION -> GraphSubscriptionMessage::class.java
                GraphMessageType.SUBSCRIPTION_RESPONSE -> GraphSubscriptionResponseMessage::class.java
                GraphMessageType.SUBSCRIPTION_UPDATE -> GraphSubscriptionUpdateMessage::class.java
                GraphMessageType.COMMAND -> GraphCommandMessage::class.java
                GraphMessageType.COMMAND_RESPONSE -> GraphCommandResponseMessage::class.java
            }
            if (targetType.rawType != messageClass) {
                return null
            }
            val adapter = moshi.nextAdapter<GraphMessage>(factory, messageClass, emptySet())
            return adapter.fromJson(readerCopy)
        }

        override fun toJson(writer: JsonWriter, value: GraphMessage?) {
            if (value == null) {
                return
            }
            val adapter = moshi.nextAdapter<GraphMessage>(factory, value::class.java, emptySet())
            return adapter.toJson(writer, value)
        }
    }

    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) {
            return null
        }
        if (!GraphMessage::class.java.isAssignableFrom(type.rawType)) {
            return null
        }
        return GraphMessageJsonAdapter(moshi = moshi, factory = this, targetType = type)
    }

}

// MARK: Data types

abstract class GraphMessage(
    @property:JsonMessageType
    @property:Json(name = "type") var type: MessageType = MessageType.GRAPH,
    @property:JsonGraphMessageType
    @property:Json(name = "subtype") var subtype: GraphMessageType,
    @property:Json(name = "id") open var id: String,
    @property:Json(name = "respondingToMessageId") open var respondingToMessageId: String? = null,
)

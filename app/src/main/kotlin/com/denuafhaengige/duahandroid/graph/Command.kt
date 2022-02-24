package com.denuafhaengige.duahandroid.graph

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

// MARK: Annotations

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonGraphCommandType

// MARK: Enums

enum class GraphCommandType(val stringValue: String) {
    HANDLES_CONTENT_ACCESS_LEVEL("handles_content_access_level")
}

// MARK: Adapters

class GraphCommandTypeAdapter {
    @ToJson
    fun toJson(@JsonGraphCommandType value: GraphCommandType): String {
        return value.stringValue
    }
    @FromJson
    @JsonGraphCommandType
    fun fromJson(value: String): GraphCommandType? {
        return GraphCommandType.values().firstOrNull { candidate -> candidate.stringValue == value }
    }
}

// MARK: Data Types

data class GraphCommandMessage(
    override var id: String,
    @JsonGraphCommandType
    val commandType: GraphCommandType,
): GraphMessage(
    id = id,
    subtype = GraphMessageType.COMMAND,
)

data class GraphCommandResponseMessage(
    override var id: String,
    override var respondingToMessageId: String?,
    val result: Boolean,
): GraphMessage(
    id = id,
    respondingToMessageId = respondingToMessageId,
    subtype = GraphMessageType.COMMAND_RESPONSE,
)

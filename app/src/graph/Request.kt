package dk.denuafhaengige.android.graph

import java.util.*

data class GraphRequestMessage(
    override var id: String = UUID.randomUUID().toString(),
    val source: String,
    val variables: Map<String, Any>?,
): GraphMessage(
    id = id,
    subtype = GraphMessageType.REQUEST,
)

data class GraphRequestResponseMessage(
    override var id: String,
    override var respondingToMessageId: String?,
    val result: GraphConnectionResult<*>?,
    val error: String?,
): GraphMessage(
    id = id,
    subtype = GraphMessageType.REQUEST_RESPONSE,
    respondingToMessageId = respondingToMessageId,
)
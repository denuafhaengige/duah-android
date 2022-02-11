package com.denuafhaengige.duahandroid.graph

data class GraphChannelConnectionRequest(
    override var queryArgs: QueryArgs? = null,
    override var pagination: Pagination? = null,
    override var includeDeleted: Boolean = true,
): GraphConnectionRequest(
    queryArgs = queryArgs,
    pagination = pagination,
    includeDeleted = includeDeleted,
) {
    override val nodeQueryInputType = "QueryChannelInputType"
    override val schemaPath = "channels"
    override val queryProperties = """
        id,
        identifier,
        title,
        hidden,
        isBroadcasting,
        squareImageFile {
            mimeType,
            type,
            url,
            path,
        },
        wideImageFile {
            mimeType,
            type,
            url,
            path,
        },
        currentBroadcast {
            id,
        },
    """.trimIndent()
}
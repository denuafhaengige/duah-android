package com.denuafhaengige.duahandroid.graph

data class GraphProgramsConnectionRequest(
    override var queryArgs: QueryArgs? = null,
    override var pagination: Pagination? = null,
    override var includeDeleted: Boolean = true,
): GraphConnectionRequest(
    queryArgs = queryArgs,
    pagination = pagination,
    includeDeleted = includeDeleted,
) {
    override val nodeQueryInputType = "QueryProgramInputType"
    override val schemaPath = "programs"
    override val queryProperties = """
        id,
        identifier,
        title,
        hostEmployees {
            id,
        },
        description,
        coverImageFile {
            mimeType,
            type,
            url,
            path,
        },
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
        hidden,
        contentAccessLevel,
        """.trimIndent()
}
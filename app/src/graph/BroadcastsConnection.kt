package dk.denuafhaengige.android.graph

data class GraphBroadcastConnectionRequest(
    override var queryArgs: QueryArgs? = null,
    override var pagination: Pagination? = null,
    override var includeDeleted: Boolean = true,
): GraphConnectionRequest(
    queryArgs = queryArgs,
    pagination = pagination,
    includeDeleted = includeDeleted,
) {
    override val nodeQueryInputType = "QueryBroadcastInputType"
    override val schemaPath = "broadcasts"
    override val queryProperties = """
        id,
        season,
        number,
        title,
        duration,
        broadcasted,
        description,
        hidden,
        program {
            id,
        },
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
        hostEmployees {
            id,
        },
        vodFile {
            mimeType,
            type,
            url,
            path,
        },
        vodDirectFile {
            mimeType,
            type,
            url,
            path,
        },
        vodSegmentedFolder {
            mimeType,
            type,
            url,
            path,
        },
        vodSingleFileFolder {
            mimeType,
            type,
            url,
            path,
        },
        """.trimIndent()
}
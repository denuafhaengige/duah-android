package dk.denuafhaengige.android.graph

data class GraphEmployeeConnectionRequest(
    override var queryArgs: QueryArgs? = null,
    override var pagination: Pagination? = null,
    override var includeDeleted: Boolean = true,
): GraphConnectionRequest(
    queryArgs = queryArgs,
    pagination = pagination,
    includeDeleted = includeDeleted,
) {
    override val nodeQueryInputType = "QueryEmployeeInputType"
    override val schemaPath = "employees"
    override val queryProperties = """
        id,
        firstName,
        lastName,
        email,
        type,
        hidden,
        photoFile {
            mimeType,
            type,
            url,
            path,
        },
    """.trimIndent()
}
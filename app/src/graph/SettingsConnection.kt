package dk.denuafhaengige.android.graph

data class GraphSettingsConnectionRequest(
    override var queryArgs: QueryArgs? = null,
    override var pagination: Pagination? = null,
    override var includeDeleted: Boolean = true,
): GraphConnectionRequest(
    queryArgs = queryArgs,
    pagination = pagination,
    includeDeleted = includeDeleted,
) {
    override val nodeQueryInputType = "QuerySettingInputType"
    override val schemaPath = "settings"
    override val queryProperties = """
        id
        identifier
        value
        """.trimIndent()
}
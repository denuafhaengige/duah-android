package dk.denuafhaengige.android.graph

import dk.denuafhaengige.android.models.*
import dk.denuafhaengige.android.util.Log
import com.squareup.moshi.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

sealed class Pagination {
    data class Forward(val first: Int? = null, val after: String? = null): Pagination()
    data class Backward(val last: Int? = null, val before: String): Pagination()
}

data class QueryArgs(
    val nodeQuery: Map<String, Any?>? = null,
    val metaDataQuery: Map<String, Date?>? = null,
)

abstract class GraphConnectionRequest(open var queryArgs: QueryArgs?, open var pagination: Pagination?, open var includeDeleted: Boolean) {

    abstract val nodeQueryInputType: String
    abstract val queryProperties: String
    abstract val schemaPath: String

    fun source(): String {
        return """
            query (${"$"}nodeQuery: $nodeQueryInputType, ${"$"}metaDataQuery: EdgeMetaDataQuery, ${"$"}first: Int, ${"$"}after: String, ${"$"}last: Int, ${"$"}before: String, ${"$"}includeDeleted: Boolean) {
                $schemaPath {
                    connection(nodeQuery: ${"$"}nodeQuery, metaDataQuery: ${"$"}metaDataQuery, first: ${"$"}first, after: ${"$"}after, last: ${"$"}last, before: ${"$"}before, includeDeleted: ${"$"}includeDeleted) {
                        edges {
                            cursor
                            node {
                                $queryProperties
                            }
                            metaData {
                                createdAt
                                updatedAt
                                deletedAt
                            }
                        }
                        metaData {
                            queryRunAt
                        }
                        pageInfo {
                            hasPreviousPage
                            hasNextPage
                            startCursor
                            endCursor
                        }
                    }
                }
            }
        """.trimIndent()
    }

    fun variables(): Map<String, Any> {
        val variables = mutableMapOf<String, Any>()
        variables["includeDeleted"] = includeDeleted
        if (queryArgs?.nodeQuery != null) {
            variables["nodeQuery"] = queryArgs!!.nodeQuery!!
        }
        if (queryArgs?.metaDataQuery != null) {
            variables["metaDataQuery"] = queryArgs!!.metaDataQuery!!
        }
        if (pagination != null) {
            when (pagination) {
                is Pagination.Backward -> {
                    val backward = pagination as Pagination.Backward
                    if (backward.last != null) {
                        variables["last"] = backward.last
                    }
                    variables["before"] = backward.before
                }
                is Pagination.Forward -> {
                    val forward = pagination as Pagination.Forward
                    if (forward.first != null) {
                        variables["first"] = forward.first
                    }
                    if (forward.after != null) {
                        variables["after"] = forward.after
                    }
                }
            }
        }
        return variables
    }
}

data class GraphConnectionMetaData(
    val queryRunAt: Date,
)

data class GraphConnectionPageInfo(
    val hasPreviousPage: Boolean,
    val hasNextPage: Boolean,
    val startCursor: String?,
    val endCursor: String?,
)

data class GraphConnectionEdge<T: Entity>(
    val cursor: String,
    val metaData: EntityMetaData,
    val node: T,
)

data class GraphConnectionResult<T: Entity>(
    val metaData: GraphConnectionMetaData,
    val pageInfo: GraphConnectionPageInfo,
    val edges: List<GraphConnectionEdge<T>>
)

class GraphConnectionResultJsonAdapterFactory: JsonAdapter.Factory {

    class GraphConnectionResultJsonAdapter(val moshi: Moshi, val factory: JsonAdapter.Factory): JsonAdapter<GraphConnectionResult<*>>() {

        override fun fromJson(reader: JsonReader): GraphConnectionResult<*>? {
            stepIntoData(reader)
            val subType = identifyTypeAndStepIntoConnection(reader)
            val type = Types.newParameterizedType(GraphConnectionResult::class.java, subType)
            val adapter = moshi.nextAdapter<GraphConnectionResult<*>>(factory, type, emptySet())
            val connection = adapter.fromJson(reader)
            for (i in 1..3) reader.endObject()
            return connection
        }

        private fun stepIntoData(reader: JsonReader) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
                throw Throwable("Expected BEGIN_OBJECT")
            }
            reader.beginObject()
            if (reader.nextName() != "data") {
                throw Throwable("Found invalid key")
            }
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
                throw Throwable("Expected BEGIN_OBJECT")
            }
        }

        private fun typeForPath(path: String): Type? {
            return when (path) {
                "broadcasts" -> Broadcast::class.java
                "channels" -> Channel::class.java
                "programs" -> Program::class.java
                "employees" -> Employee::class.java
                "settings" -> Setting::class.java
                else -> null
            }
        }

        private fun identifyTypeAndStepIntoConnection(reader: JsonReader): Type {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
                throw Throwable("Expected BEGIN_OBJECT")
            }
            reader.beginObject()
            val path = reader.nextName()
            val type = typeForPath(path) ?: throw Throwable("Unable to identify type for path: $path")
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
                throw Throwable("Expected BEGIN_OBJECT")
            }
            reader.beginObject()
            if (reader.nextName() != "connection") {
                throw Throwable("Expected connection key")
            }
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
                throw Throwable("Expected BEGIN_OBJECT")
            }
            return type
        }

        override fun toJson(writer: JsonWriter, value: GraphConnectionResult<*>?) {
            throw Throwable("toJson not supported")
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
        if (type !is ParameterizedType) {
            return null
        }
        if (type.rawType != GraphConnectionResult::class.java) {
            return null
        }
        return GraphConnectionResultJsonAdapter(moshi = moshi, factory = this)
    }

}

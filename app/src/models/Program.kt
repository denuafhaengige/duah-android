package com.denuafhaengige.duahandroid.models

import android.net.Uri
import androidx.room.*
import com.squareup.moshi.Json
import com.denuafhaengige.duahandroid.content.ContentStore
import com.denuafhaengige.duahandroid.content.EntityFlow
import java.util.*
import kotlin.random.Random

@androidx.room.Entity
data class Program(
    @PrimaryKey
    override val id: Int,
    override val title: String,
    override val description: String?,
    @Embedded(prefix = "square_image_file_")
    val squareImageFile: File?,
    @Embedded(prefix = "wide_image_file_")
    val wideImageFile: File?,
    val hidden: Boolean,
): Entity, Titled, Imaged, Described {

    companion object {
        val example
            get() = Program(
                id = Random(1000).nextInt(),
                title = "En uafhænging morgen",
                description = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable.",
                hidden = false,
                squareImageFile = File(
                    path = "",
                    url = "https://graph.denuafhaengige.dk/files/images/eum.png",
                ),
                wideImageFile = File(
                    path = "",
                    url = "https://graph.denuafhaengige.dk/files/images/eum_bred.png",
                ),
            )
    }

    @Ignore
    @Json(name = "hostEmployees")
    var hostEmployeeReferences: List<EntityReference<Employee>> = emptyList()

    override val wideImageUri: Uri?
        get() {
            val string = wideImageFile?.url ?: return null
            return Uri.parse(string)
        }

    override val squareImageUri: Uri?
        get() {
            val string = squareImageFile?.url ?: return null
            return Uri.parse(string)
        }

}

@Dao
interface ProgramDao {
    @Query("SELECT * FROM program")
    suspend fun getAll(): List<Program>

    @Query("SELECT * FROM program WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<Program>

    @Query("SELECT * FROM program WHERE title LIKE :title")
    suspend fun findByTitle(title: String): List<Program>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<Program>)

    @Delete
    suspend fun delete(entity: Program)

    @Delete
    suspend fun deleteAll(entities: List<Program>)
}

class ProgramFetcher(
    private val store: ContentStore
): EntityFlow.Fetcher<Program> {
    override suspend fun get(id: Int): Program? =
        store.database.programDao()
            .loadAllByIds(ids = intArrayOf(id))
            .firstOrNull()
}

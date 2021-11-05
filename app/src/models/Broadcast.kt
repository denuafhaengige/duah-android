package dk.denuafhaengige.android.models

import android.net.Uri
import androidx.room.*
import com.squareup.moshi.Json
import dk.denuafhaengige.android.content.ContentStore
import dk.denuafhaengige.android.content.EntityFlow
import dk.denuafhaengige.android.util.DurationFormatter
import java.util.*
import kotlin.random.Random

@androidx.room.Entity
data class Broadcast(
    @PrimaryKey
    override val id: Int,
    val season: Int? = null,
    val number: Int? = null,
    override val title: String,
    val broadcasted: Date? = null,
    override val description: String? = null,
    val hidden: Boolean = false,
    val duration: Int? = null,
    @Embedded(prefix = "square_image_file_")
    val squareImageFile: File? = null,
    @Embedded(prefix = "wide_image_file_")
    val wideImageFile: File? = null,
    @Embedded(prefix = "vod_segmented_folder_")
    val vodSegmentedFolder: File? = null,
    @Embedded(prefix = "vod_single_file_folder_")
    val vodSingleFileFolder: File? = null,
    @Embedded(prefix = "vod_direct_file_")
    val vodDirectFile: File? = null,
    @ColumnInfo(name = "program_id")
    @Json(name = "program")
    val programReference: EntityReference<Program>? = null,
): Entity, Titled, Imaged, Described {

    companion object {
        val example
            get() = Broadcast(
                id = Random(1000).nextInt(),
                broadcasted = Date(),
                title = "En god udsendelse, som er værd at høre",
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
interface BroadcastDao {

    @Transaction
    @Query("SELECT * FROM broadcast")
    suspend fun getAll(): List<BroadcastWithProgramAndEmployees>

    @Transaction
    @Query("SELECT * FROM broadcast WHERE hidden = 0 AND program_id IS NOT null ORDER BY broadcasted DESC LIMIT :limit")
    suspend fun getRecentNonHiddenWithProgram(limit: Int): List<BroadcastWithProgramAndEmployees>

    @Transaction
    @Query("SELECT * FROM broadcast WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<BroadcastWithProgramAndEmployees>

    @Transaction
    @Query("SELECT * FROM broadcast WHERE title LIKE :title")
    suspend fun findByTitle(title: String): List<BroadcastWithProgramAndEmployees>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<Broadcast>)

    @Delete
    suspend fun delete(entity: Broadcast)

    @Delete
    suspend fun deleteAll(entities: List<Broadcast>)
}

data class BroadcastWithProgramAndEmployees(
    @Embedded val broadcast: Broadcast,
    @Relation(
        parentColumn = "program_id",
        entityColumn = "id"
    )
    val program: Program?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            BroadcastEmployeeCrossRef::class,
            parentColumn = "broadcast_id",
            entityColumn = "employee_id",
        ),
    )
    val employees: List<Employee>,
): Titled, MetaTitled, Imaged, Described, Entity {

    override val id
        get() = broadcast.id

    companion object {
        val example
            get() = BroadcastWithProgramAndEmployees(
                broadcast = Broadcast.example,
                program = Program.example,
                employees = listOf(Employee.example, Employee.example),
            )
    }

    override val wideImageUri: Uri?
        get() {
            val broadcastImage = broadcast.wideImageUri
            if (broadcastImage != null) {
                return broadcastImage
            }
            return program?.wideImageUri
        }

    override val squareImageUri: Uri?
        get() {
            val broadcastImage = broadcast.squareImageUri
            if (broadcastImage != null) {
                return broadcastImage
            }
            return program?.squareImageUri
        }

    override val title: String
        get() = broadcast.title

    override val description: String?
        get() = broadcast.description

    override val metaTitle: String?
        get() = program?.title

    override val metaTitleSupplement: String?
        get() =
            if (broadcast.duration != null) DurationFormatter.secondsToHoursMinutes(broadcast.duration)
            else null
}

class BroadcastFetcher(
    private val store: ContentStore
): EntityFlow.Fetcher<BroadcastWithProgramAndEmployees> {
    override suspend fun get(id: Int): BroadcastWithProgramAndEmployees? =
        store.database.broadcastDao()
            .loadAllByIds(ids = intArrayOf(id))
            .firstOrNull()
}

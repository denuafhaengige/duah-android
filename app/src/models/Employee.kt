package dk.denuafhaengige.android.models

import androidx.room.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import kotlin.random.Random

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonEmployeeType

enum class EmployeeType(val stringValue: String) {
    HOST("host"),
    VOLUNTEER("volunteer"),
    OTHER("other"),
    UNKNOWN("unknown"),
}

class EmployeeTypeAdapter {

    @TypeConverter
    @ToJson
    fun toJson(@JsonEmployeeType value: EmployeeType): String {
        return value.stringValue
    }

    @TypeConverter
    @FromJson
    @JsonEmployeeType
    fun fromJson(value: String): EmployeeType? {
        val derived = EmployeeType.values().firstOrNull { candidate -> candidate.stringValue == value }
        return derived ?: EmployeeType.UNKNOWN
    }
}

@androidx.room.Entity
data class Employee(
    @PrimaryKey
    override val id: Int,
    @JsonEmployeeType
    val type: EmployeeType,
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String,
    val email: String? = null,
    val title: String? = null,
    val description: String? = null,
    @Embedded(prefix = "photo_file_")
    val photoFile: File? = null,
    val sort: Int? = null,
    val hidden: Boolean,
): Entity {
    companion object {
        val example
            get() = Employee(
                id = Random(1000).nextInt(),
                type = EmployeeType.HOST,
                firstName = "Asger",
                lastName = "Juhl",
                hidden = false,
                photoFile = File(
                    path = "",
                    url = "https://graph.denuafhaengige.dk/files/images/employees/asger%20ny%20500%20px.jpg",
                ),
            )
    }
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employee")
    suspend fun getAll(): List<Employee>

    @Query("SELECT * FROM employee WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<Employee>

    @Query("SELECT * FROM employee WHERE first_name LIKE :firstName AND last_name LIKE :lastName LIMIT 1")
    suspend fun findByTitle(firstName: String, lastName: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<Employee>)

    @Delete
    suspend fun delete(entity: Employee)

    @Delete
    suspend fun deleteAll(entities: List<Employee>)
}

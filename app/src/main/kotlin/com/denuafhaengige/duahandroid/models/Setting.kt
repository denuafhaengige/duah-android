package com.denuafhaengige.duahandroid.models

import androidx.room.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import dev.zacsweers.moshix.adapters.JsonString

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonSettingIdentifier

class SettingIdentifierAdapter {
    @TypeConverter
    @ToJson
    fun toJson(@JsonSettingIdentifier value: SettingIdentifier): String {
        return value.stringValue
    }
    @TypeConverter
    @FromJson
    @JsonSettingIdentifier
    fun fromJson(value: String): SettingIdentifier? {
        return SettingIdentifier.values().firstOrNull { candidate -> candidate.stringValue == value }
    }
}

enum class SettingIdentifier(val stringValue: String) {
    FEATURED("featured"),
    PLACEHOLDER_IMAGE_URL("placeholderImageURL"),
    APP_DO_FULL_LOAD_AFTER("appDoFullLoadAfter"),
}

@androidx.room.Entity
data class Setting(
    @PrimaryKey
    override val id: Int,
    val identifier: String,
    @JsonString
    val value: String,
): Entity

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting")
    suspend fun getAll(): List<Setting>

    @Query("SELECT * FROM setting WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<Setting>

    @Query("SELECT * FROM setting WHERE identifier LIKE :identifier")
    suspend fun findByIdentifier(identifier: String): List<Setting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<Setting>)

    @Delete
    suspend fun delete(entity: Setting)

    @Delete
    suspend fun deleteAll(entities: List<Setting>)
}

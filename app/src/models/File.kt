package com.denuafhaengige.duahandroid.models

import androidx.room.ColumnInfo
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonFileType

enum class FileType(val stringValue: String) {
    FIlE("File"),
    DIRECTORY("Directory"),
}

class FileTypeAdapter {
    @TypeConverter
    @ToJson
    fun toJson(@JsonFileType value: FileType): String {
        return value.stringValue
    }
    @TypeConverter
    @FromJson
    @JsonFileType
    fun fromJson(value: String): FileType? {
        return FileType.values().firstOrNull { candidate -> candidate.stringValue == value }
    }
}

data class File(
    val url: String? = null,
    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,
    @JsonFileType
    val type: FileType = FileType.FIlE,
    val path: String,
)

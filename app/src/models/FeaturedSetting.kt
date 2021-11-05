package com.denuafhaengige.duahandroid.models

import androidx.room.TypeConverter
import com.squareup.moshi.*

class FeaturedSetting {

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    annotation class JsonType

    companion object {
        const val Identifier = "featured"
    }

    enum class ValueType(val stringValue: String) {
        BROADCAST("broadcast"),
        PROGRAM("program"),
        CHANNEL("channel"),
    }

    class ValueTypeAdapter {
        @TypeConverter
        @ToJson
        fun toJson(@JsonType value: ValueType): String {
            return value.stringValue
        }
        @TypeConverter
        @FromJson
        @JsonType
        fun fromJson(value: String): ValueType? {
            return ValueType.values().firstOrNull { candidate -> candidate.stringValue == value }
        }
    }

    data class Value(
        @JsonType
        val type: ValueType,
        val id: Int,
    )

}
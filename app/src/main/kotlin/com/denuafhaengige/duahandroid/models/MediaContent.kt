package com.denuafhaengige.duahandroid.models

import android.net.Uri
import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

interface Identifiable {
    val id: Int
}

interface Titled {
    val title: String
}

interface MetaTitled {
    val metaTitle: String?
    val metaTitleSupplement: String?
}

interface Described {
    val description: String?
}

interface Imaged {
    val wideImageUri: Uri?
    val squareImageUri: Uri?
}

interface AccessControlled {
    val contentAccessLevel: ContentAccessLevel?
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonContentAccessLevel

enum class ContentAccessLevel(val stringValue: String) {
    FREE("free"),
    FREE_WITH_ADS("free_with_ads"),
    MEMBERS_ONLY("members_only"),
}

class ContentAccessLevelAdapter {

    @TypeConverter
    @ToJson
    fun toJson(@JsonContentAccessLevel value: ContentAccessLevel): String {
        return value.stringValue
    }

    @TypeConverter
    @FromJson
    @JsonContentAccessLevel
    fun fromJson(value: String): ContentAccessLevel? {
        return ContentAccessLevel.values()
            .firstOrNull { candidate -> candidate.stringValue == value }
    }
}

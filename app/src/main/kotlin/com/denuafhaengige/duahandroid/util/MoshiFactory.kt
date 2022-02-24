package com.denuafhaengige.duahandroid.util

import com.denuafhaengige.duahandroid.graph.*
import com.denuafhaengige.duahandroid.members.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.zacsweers.moshix.adapters.JsonString
import com.denuafhaengige.duahandroid.models.*
import java.util.*

class MoshiFactory {
    companion object {
        fun moshi(): Moshi {
            return Moshi.Builder()
                .add(GraphMessageJsonAdapterFactory())
                .add(GraphConnectionResultJsonAdapterFactory())
                .add(GraphSubscriptionUpdateMessagePayloadAdapterFactory())
                .add(MessageTypeAdapter())
                .add(SettingIdentifierAdapter())
                .add(JsonString.Factory())
                .add(GraphSubscriptionTypeAdapter())
                .add(GraphMessageTypeAdapter())
                .add(EntityType::class.java, EntityTypeAdapter())
                .add(FileTypeAdapter())
                .add(EmployeeTypeAdapter())
                .add(Date::class.java, Rfc3339DateJsonAdapter())
                .add(FeaturedSetting.ValueTypeAdapter())
                .add(MemberSubscriptionStatusAdapter())
                .add(MemberSubscriptionChargeFrequencyAdapter())
                .add(MemberSubscriptionTypeAdapter())
                .add(ContentAccessLevelAdapter())
                .add(GraphCommandTypeAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }
}

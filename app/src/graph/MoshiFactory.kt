package dk.denuafhaengige.android.graph

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.zacsweers.moshix.adapters.JsonString
import dk.denuafhaengige.android.models.*
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
                .add(KotlinJsonAdapterFactory())
                .build()
        }
    }
}

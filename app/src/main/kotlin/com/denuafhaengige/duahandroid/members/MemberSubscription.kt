package com.denuafhaengige.duahandroid.members

import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.*

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonMemberSubscriptionType

enum class MemberSubscriptionType(val stringValue: String) {
    MOBILE_PAY("mobilepay"),
    APPLE("apple"),
    UNKNOWN("unknown"),
}

class MemberSubscriptionTypeAdapter {
    @TypeConverter
    @ToJson
    fun toJson(@JsonMemberSubscriptionType value: MemberSubscriptionType): String {
        return value.stringValue
    }
    @TypeConverter
    @FromJson
    @JsonMemberSubscriptionType
    fun fromJson(value: String): MemberSubscriptionType? {
        val derived = MemberSubscriptionType.values().firstOrNull { candidate -> candidate.stringValue == value }
        return derived ?: MemberSubscriptionType.UNKNOWN
    }
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonMemberSubscriptionStatus

enum class MemberSubscriptionStatus(val stringValue: String) {
    PENDING("pending"),
    ACTIVE("active"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    UNKNOWN("unknown"),
}

class MemberSubscriptionStatusAdapter {
    @TypeConverter
    @ToJson
    fun toJson(@JsonMemberSubscriptionStatus value: MemberSubscriptionStatus): String {
        return value.stringValue
    }
    @TypeConverter
    @FromJson
    @JsonMemberSubscriptionStatus
    fun fromJson(value: String): MemberSubscriptionStatus? {
        val derived = MemberSubscriptionStatus.values().firstOrNull { candidate -> candidate.stringValue == value }
        return derived ?: MemberSubscriptionStatus.UNKNOWN
    }
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class JsonMemberSubscriptionChargeFrequency

enum class MemberSubscriptionChargeFrequency(val stringValue: String) {
    MONTHLY("monthly"),
    YEARLY("yearly"),
    UNKNOWN("unknown"),
}

class MemberSubscriptionChargeFrequencyAdapter {
    @TypeConverter
    @ToJson
    fun toJson(@JsonMemberSubscriptionChargeFrequency value: MemberSubscriptionChargeFrequency): String {
        return value.stringValue
    }
    @TypeConverter
    @FromJson
    @JsonMemberSubscriptionChargeFrequency
    fun fromJson(value: String): MemberSubscriptionChargeFrequency? {
        val derived = MemberSubscriptionChargeFrequency.values().firstOrNull { candidate -> candidate.stringValue == value }
        return derived ?: MemberSubscriptionChargeFrequency.UNKNOWN
    }
}

data class MemberSubscription(
    val id: Int,
    val email: String,
    @JsonMemberSubscriptionType
    val type: MemberSubscriptionType,
    @JsonMemberSubscriptionStatus
    val status: MemberSubscriptionStatus,
    @JsonMemberSubscriptionChargeFrequency
    val chargeFrequency: MemberSubscriptionChargeFrequency,
    val nextChargeDate: String? = null,
    val currency: String? = null,
    val recurringAmount: Double,
    val campaign: String? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val deletedAt: Date? = null,
) {
    companion object {
        val example: MemberSubscription
            get() = MemberSubscription(
                id = Random().nextInt(1000),
                type = MemberSubscriptionType.MOBILE_PAY,
                chargeFrequency = MemberSubscriptionChargeFrequency.MONTHLY,
                createdAt = Date(),
                updatedAt = Date(),
                email = "rasmus@hummelmose.dk",
                status = MemberSubscriptionStatus.ACTIVE,
                recurringAmount = 59.0,
                nextChargeDate = "2022-06-20",
            )
    }

    val nextChargeDateFormatted: Date?
        get() {
            val nextChargeDate = nextChargeDate ?: return null
            return SimpleDateFormat("yyyy-MM-dd").parse(nextChargeDate)
        }
    val providesAccess: Boolean
        get() {
            if (status == MemberSubscriptionStatus.ACTIVE) {
                return true
            }
            val nextChargeDate = nextChargeDateFormatted ?: return false
            if (status == MemberSubscriptionStatus.CANCELLED && nextChargeDate > Date()) {
                return true
            }
            return false
        }
}

val List<MemberSubscription>.hasAccessProvidingSubscription: Boolean
    get() = accessProviding.isNotEmpty()

val List<MemberSubscription>.accessProviding: List<MemberSubscription>
    get() = filter { it.providesAccess }
package com.denuafhaengige.duahandroid.models

import androidx.room.TypeConverter
import java.util.*

class RoomConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun entityReferenceToInt(reference: EntityReference<*>?): Int? {
        return reference?.id
    }

    @TypeConverter
    fun intToBroadcastEntityReference(int: Int?): EntityReference<Broadcast>? {
        if (int == null) {
            return null
        }
        return EntityReference(int)
    }

    @TypeConverter
    fun intToEmployeeEntityReference(int: Int?): EntityReference<Employee>? {
        if (int == null) {
            return null
        }
        return EntityReference(int)
    }

    @TypeConverter
    fun intToProgramEntityReference(int: Int?): EntityReference<Program>? {
        if (int == null) {
            return null
        }
        return EntityReference(int)
    }

    @TypeConverter
    fun intToChannelEntityReference(int: Int?): EntityReference<Channel>? {
        if (int == null) {
            return null
        }
        return EntityReference(int)
    }
}
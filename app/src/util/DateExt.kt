package com.denuafhaengige.duahandroid.util

import java.time.Instant
import java.util.*

object DateUtil {
    fun nowMinusMinutes(minutes: Long) =
        Date.from(Instant.now().minusSeconds((minutes*60)))
}

package dk.denuafhaengige.android.util

import kotlin.math.floor

abstract class DurationFormatter {

    companion object {

        private fun secondsToHoursMinutesSeconds(seconds: Int): Triple<Int, Int, Int> {
            return Triple(
                floor(seconds / 3600.0).toInt(),
                floor((seconds % 3600.0) / 60.0).toInt(),
                floor(seconds % 60.0).toInt(),
            )
        }

        fun secondsToHoursMinutes(seconds: Int): String {
            val hms = secondsToHoursMinutesSeconds(seconds)
            var result = "${hms.second} MIN"
            if (hms.first > 0) result = "${hms.first} T $result"
            return result
        }

        fun secondsToHMS(seconds: Int): String {
            val hms = secondsToHoursMinutesSeconds(seconds)
            var result =
                if (hms.third > 9) hms.third
                else "0${hms.third}"
            result =
                if (hms.second > 9) "${hms.second}:$result"
                else "0${hms.second}:$result"
            if (hms.first > 0)
                result = "${hms.first}:$result"
            return  result
        }
    }
}
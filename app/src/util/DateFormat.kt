package dk.denuafhaengige.android.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class DateFormat {
    companion object {

        fun dayMonthFormatted(locale: Locale, date: Date): String {
            val formatString =  when (locale.language) {
                "da" -> "d. MMMM"
                else -> "MMMM d"
            }
            return SimpleDateFormat(formatString, locale).format(date)
        }
    }
}
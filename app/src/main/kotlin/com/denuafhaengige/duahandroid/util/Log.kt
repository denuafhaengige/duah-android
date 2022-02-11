package com.denuafhaengige.duahandroid.util

import java.text.SimpleDateFormat
import java.util.*

object Log {

    private val format = SimpleDateFormat("HH:mm:ss.SSSS", Locale.US)

    fun debug(text: String) {
        val date = Date()
        val formatted = format.format(date)
        println("$formatted | $text")
    }
}

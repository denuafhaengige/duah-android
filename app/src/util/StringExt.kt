package com.denuafhaengige.duahandroid.util

fun String.capitalizeWords() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()
fun String.capitalizeFirstWord() = this.replaceFirstChar { it.titlecase() }

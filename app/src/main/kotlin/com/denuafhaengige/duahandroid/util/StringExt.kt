package com.denuafhaengige.duahandroid.util

fun String.capitalizeWords() = this.split(" ").joinToString(" ") { it.capitalizeFirstWord() }.trimEnd()
fun String.capitalizeFirstWord() = this.replaceFirstChar { it.titlecase() }

package com.denuafhaengige.duahandroid.theming

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorPalette = darkColors(
    primary = Color.White,
    secondary = Color.LightGray,
    background = Color.Black,
    surface = VeryDarkGrey,
    onBackground = Color.White,
    onSurface = Color.White,
)

val LightColorPalette = lightColors(
    primary = RedColor,
    secondary = Color.Gray,
    background = Color.White,
    surface = VeryLightGrey,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun DuahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

package com.denuafhaengige.duahandroid.views

import android.hardware.lights.Light
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.theming.DarkColorPalette
import com.denuafhaengige.duahandroid.theming.LightColorPalette

enum class IconLabelButtonVariant {
    ICON_LABEL,
    LABEL_ICON,
}

@Composable
fun IconLabelButton(
    variant: IconLabelButtonVariant = IconLabelButtonVariant.ICON_LABEL,
    iconPainter: Painter,
    label: String,
    contentPadding: PaddingValues = PaddingValues(horizontal = 15.dp, vertical = 5.dp),
    spacing: Dp = 6.dp,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    labelModifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    enabled: Boolean = true,
    action: () -> Unit = {},
) {
    val colors =
        if (darkTheme) DarkColorPalette
        else LightColorPalette

    val buttonColors = ButtonDefaults.buttonColors(
        backgroundColor = colors.onBackground,
        contentColor = colors.background,
        disabledBackgroundColor = colors.onBackground,
        disabledContentColor = colors.background,
    )

    Button(
        onClick = action,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = contentPadding,
        colors = buttonColors,
        enabled = enabled,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            when (variant) {
                IconLabelButtonVariant.ICON_LABEL -> {
                    IconLabelButtonIcon(painter = iconPainter, modifier = iconModifier)
                    IconLabelButtonLabel(text = label, modifier = labelModifier)
                }
                IconLabelButtonVariant.LABEL_ICON -> {
                    IconLabelButtonLabel(text = label, modifier = labelModifier)
                    IconLabelButtonIcon(painter = iconPainter, modifier = iconModifier)
                }
            }
        }
    }
}

@Composable
private fun IconLabelButtonIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painter,
        contentDescription = "Decorative",
        modifier = modifier
            .aspectRatio(1F),
    )
}

@Composable
private fun IconLabelButtonLabel(
    text: String,
    textStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    ),
    modifier: Modifier = Modifier,
) {
    Text(
        maxLines = 1,
        style = textStyle,
        text = text,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun IconLabelButtonPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconLabelButton(
            iconPainter = painterResource(id = R.drawable.ic_play),
            iconModifier = Modifier
                .scale(.8F),
            label = "PLAY",
            modifier = Modifier
                .height(30.dp),
        )
        IconLabelButton(
            iconPainter = rememberVectorPainter(image = Icons.Default.Person),
            label = "Log in",
            modifier = Modifier
                .height(30.dp),
        )
        IconLabelButton(
            iconPainter = painterResource(id = R.drawable.ic_play),
            iconModifier = Modifier
                .scale(.8F),
            label = "PLAY",
            modifier = Modifier
                .height(30.dp),
        )
        IconLabelButton(
            iconPainter = painterResource(id = R.drawable.ic_play),
            iconModifier = Modifier
                .scale(.8F),
            label = "PLAY",
            modifier = Modifier
                .height(30.dp),
        )
    }
}

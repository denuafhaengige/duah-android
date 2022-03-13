package com.denuafhaengige.duahandroid.views

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.Described
import com.denuafhaengige.duahandroid.models.Imaged
import com.denuafhaengige.duahandroid.models.MetaTitled
import com.denuafhaengige.duahandroid.models.Titled
import com.denuafhaengige.duahandroid.theming.LighterDarkGrey
import com.denuafhaengige.duahandroid.theming.VeryDarkerGrey
import com.denuafhaengige.duahandroid.theming.VeryLighterGrey
import com.denuafhaengige.duahandroid.util.capitalizeWords

object ContentDimensions {
    val squareBannerSize = 150.dp
    val contentRowVerticalPadding = 30.dp
}

@Composable
fun MetaTitleTextForContent(content: MetaTitled, modifier: Modifier = Modifier) {
    content.metaTitle?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary,
            modifier = modifier,
        )
    }
}

@Composable
fun MetaTitleSupplementTextForContent(content: MetaTitled, modifier: Modifier = Modifier) {
    content.metaTitleSupplement?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary,
            modifier = modifier,
        )
    }
}

@Composable
fun MetaTextForContent(content: MetaTitled) {
    Text(
        text = annotatedMetaTextForContent(content = content),
        style = MaterialTheme.typography.caption,
        maxLines = 1
    )
}

@Composable
fun LargeTitleTextForContent(content: Titled, maxLines: Int = Int.MAX_VALUE) {
    Text(
        text = content.title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
    )
}

@Composable
fun SmallTitleTextForContent(
    content: Titled,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = content.title,
        style = MaterialTheme.typography.subtitle2,
        modifier = modifier,
        maxLines = maxLines,
    )
}

@Composable
fun DescriptionTextForContent(content: Described, maxLines: Int = Int.MAX_VALUE) {
    content.description?.let {
        Text(
            text = it,
            fontSize = 14.sp,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

enum class ContentImageVariant {
    WIDE,
    SQUARE,
}

@Composable
fun appBarBackgroundColor(): Color {
    return if (isSystemInDarkTheme()) VeryDarkerGrey
    else VeryLighterGrey
}

enum class AppBarBorderVariant {
    TOP,
    BOTTOM,
}

@Composable
fun Modifier.drawAppBarBorderBehind(variant: AppBarBorderVariant) = drawBehind {
    val y = when (variant) {
        AppBarBorderVariant.BOTTOM -> size.height - Dp.Hairline.value
        AppBarBorderVariant.TOP -> 0F
    }
    drawLine(
        color = LighterDarkGrey,
        start = Offset(0F, y),
        end = Offset(size.width, y),
        strokeWidth = Dp.Hairline.value,
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun imagePainterForContent(
    content: Imaged,
    variant: ContentImageVariant = ContentImageVariant.SQUARE
): Painter {

    val darkTheme = isSystemInDarkTheme()

    val fallbackImageId = when {
        darkTheme && variant == ContentImageVariant.SQUARE -> R.drawable.logo_black_on_white
        darkTheme && variant == ContentImageVariant.WIDE -> R.drawable.banner_black_on_white
        variant == ContentImageVariant.SQUARE -> R.drawable.logo_white_on_black
        variant == ContentImageVariant.WIDE -> R.drawable.logo_black_on_white
        else -> throw Throwable("Shut up")
    }

    val imagePainter = when {
        variant == ContentImageVariant.SQUARE && content.squareImageUri != null ->
            rememberImagePainter(content.squareImageUri)
        variant == ContentImageVariant.SQUARE ->
            painterResource(id = fallbackImageId)
        variant == ContentImageVariant.WIDE && content.wideImageUri != null ->
            rememberImagePainter(content.wideImageUri)
        variant == ContentImageVariant.WIDE ->
            painterResource(id = fallbackImageId)
        else -> throw Throwable("Shut up")
    }

    return imagePainter
}

@Composable
fun casedStringResource(@StringRes id: Int): String {
    return stringResource(id).replaceFirstChar { it.titlecase() }
}

@Composable
fun annotatedMetaTextForContent(content: MetaTitled) = buildAnnotatedString {
    withStyle(
        SpanStyle(
            color = MaterialTheme.colors.primary,
        )
    ) {
        val metaTitle =
            content.metaTitle ?:
            stringResource(id = R.string.fallback_program_title).capitalizeWords()
        append(metaTitle)
    }
    content.metaTitleSupplement?.let {
        withStyle(
            SpanStyle(
                color = MaterialTheme.colors.secondary,
            )
        ) {
            append(" | $it")
        }
    }
}

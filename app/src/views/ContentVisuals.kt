package com.denuafhaengige.duahandroid.views

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.Described
import com.denuafhaengige.duahandroid.models.MetaTitled
import com.denuafhaengige.duahandroid.models.Titled
import com.denuafhaengige.duahandroid.util.capitalizeWords

object ContentDimensions {
    val wideBannerHeight = 200.dp
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

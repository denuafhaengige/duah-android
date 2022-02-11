package com.denuafhaengige.duahandroid.views

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.denuafhaengige.duahandroid.models.Employee

@OptIn(ExperimentalCoilApi::class)
@Composable
fun CircleImage(uri: Uri, borderColor: Color, modifier: Modifier = Modifier) {

    val maxBorderWidth = 4.dp

    BoxWithConstraints(modifier = modifier) {

        val relativeBorderWidth = maxWidth.div(18)
        val borderWidth =
            if (relativeBorderWidth > maxBorderWidth) maxBorderWidth
            else relativeBorderWidth

        Image(
            painter = rememberImagePainter(uri),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(
                    border = BorderStroke(borderWidth, color = borderColor),
                    shape = CircleShape,
                )
                .padding(borderWidth),
        )
    }
}

@Preview
@Composable
fun CircleImagePreview() {
    val uri = Uri.parse(Employee.example.photoFile!!.url)
    CircleImage(
        uri = uri,
        borderColor = Color.Black,
        modifier = Modifier
            .size(80.dp),
    )
}

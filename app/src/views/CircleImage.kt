package dk.denuafhaengige.android.views

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import dk.denuafhaengige.android.models.Employee

@Composable
fun CircleImage(uri: Uri, diameter: Dp, borderColor: Color, modifier: Modifier? = null) {
    val maxBorderWidth = 4.dp
    val relativeBorderWidth = diameter.div(18)
    val borderWidth = if (relativeBorderWidth > maxBorderWidth)
        maxBorderWidth
        else relativeBorderWidth
    val boxModifier = modifier ?: Modifier
    Box(boxModifier) {
        Image(
            painter = rememberImagePainter(uri),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(diameter)
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
        diameter = 80.dp,
        borderColor = Color.Black,
    )
}

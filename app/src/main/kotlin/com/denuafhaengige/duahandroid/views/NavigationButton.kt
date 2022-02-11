package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denuafhaengige.duahandroid.R

@Composable
private fun navigationButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colors.onBackground,
    contentColor = MaterialTheme.colors.background,
)

@Composable
fun NavigationButton(
    text: String,
    modifier: Modifier = Modifier,
    action: () -> Unit = {},
) {

    val colors = navigationButtonColors()

    Button(
        onClick = action,
        modifier = modifier
            .height(30.dp),
        colors = colors,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .padding(start = 15.dp),
        )
        Icon(
            painter = rememberVectorPainter(image = Icons.Default.ChevronRight),
            contentDescription = "Decorative",
            modifier = Modifier
                .size(30.dp),
        )
    }
}

@Preview
@Composable
fun NavigationButtonPreview() {

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(30.dp)
        ) {
            NavigationButton(text = "ALLE")
            NavigationButton(text = "Alle")
            NavigationButton(text = "En masse")
            NavigationButton(text = "Flere FORSKELLIGE")
            NavigationButton(text = "HEJ")
            NavigationButton(text = "med dig der")
        }

}

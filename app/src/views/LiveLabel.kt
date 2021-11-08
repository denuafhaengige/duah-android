package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveLabel() {
    Row(
        modifier = Modifier
            .size(width = 60.dp, height = 25.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(30.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(8.dp)
                .background(Color.Black, shape = CircleShape),
        )
        Text(
            text = "LIVE",
            style = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
            ),
        )
    }
}

@Preview
@Composable
private fun LiveLabelPreview() {
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 80.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        LiveLabel()
    }
}

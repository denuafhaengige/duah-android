package dk.denuafhaengige.android.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.denuafhaengige.android.util.DateFormat
import java.util.*

@Composable
fun DayMonthLabel(date: Date) {

    val locale = LocalContext.current.resources.configuration.locales[0]
    val formattedDate = DateFormat.dayMonthFormatted(locale, date)

    Box(
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.5F),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 10.dp
                ),
            )
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.caption,
            color = Color.White,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp,
            ),
        )
    }

}

@Preview
@Composable
fun DayMonthLabelPreview() {
    DayMonthLabel(date = Date())
}

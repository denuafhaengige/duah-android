package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees
import com.denuafhaengige.duahandroid.theming.DuahTheme

@Composable
fun ContentRow(
    title: String,
    modifier: Modifier = Modifier,
    titleFarRightItem: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h1,
            )
            titleFarRightItem()
        }
        content()
    }
}

@Preview
@Composable
private fun ContentRowPreview() {
    DuahTheme {
        ContentRow(
            title = "Meget lækkert indhold",
            titleFarRightItem = {
                NavigationButton(text = "VIDERE")
            },
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
            ) {
                ContentRowPreviewDummyItem()
                ContentRowPreviewDummyItem()
                ContentRowPreviewDummyItem()
            }
        }
    }
}

@Composable
private fun ContentRowPreviewDummyItem() {
    BroadcastVisual(
        broadcast = BroadcastWithProgramAndEmployees.example,
        style = BroadcastVisualStyle.SQUARE,
        modifier = Modifier
            .size(ContentDimensions.squareBannerSize)
    )
}

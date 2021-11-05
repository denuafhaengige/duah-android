package com.denuafhaengige.duahandroid.views

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.*
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.content.Featured
import com.denuafhaengige.duahandroid.models.BroadcastWithProgramAndEmployees
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.player.Playable
import com.denuafhaengige.duahandroid.player.PlayerViewModel
import com.denuafhaengige.duahandroid.util.LiveFeatured
import com.denuafhaengige.duahandroid.util.capitalizeWords

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FeaturedPager(content: List<LiveFeatured>, playerViewModel: PlayerViewModel) {

    val state = rememberPagerState(pageCount = content.size)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(365.dp)
    ) {
        HorizontalPager(
            state,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val item = content[page]
            FeaturedPagerItem(liveFeatured = item, playerViewModel = playerViewModel)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalPagerIndicator(
                pagerState = state,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }


}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FeaturedPagerItem(liveFeatured: LiveFeatured, playerViewModel: PlayerViewModel) {

    val featured by liveFeatured.liveFeatured.observeAsState()

    val metaTitle =
        featured?.metaTitle ?:
        stringResource(id = R.string.fallback_program_title).capitalizeWords()

    val metaTitleSupplement = featured?.metaTitleSupplement

    val metaText = buildAnnotatedString {
        withStyle(SpanStyle(
            color = MaterialTheme.colors.primary,
        )) {
            append(metaTitle)
        }
        if (metaTitleSupplement != null) {
            withStyle(SpanStyle(
                color = MaterialTheme.colors.secondary,
            )) {
                append(" | $metaTitleSupplement")
            }
        }
    }

    val description = featured?.description

    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()) {
        featured?.let {
            when (it) {
                is Featured.Broadcast ->
                    BroadcastVisual(
                        playableBroadcast = it.playable as Playable.Broadcast,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        hostPhotoDiameter = 60.dp,
                        style = BroadcastVisualStyle.WIDE,
                        playerViewModel = playerViewModel,
                    )
                else -> {}
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = metaText, style = MaterialTheme.typography.caption, maxLines = 1)
            featured?.title?.let {
                Text(text = it, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 2)
            }
            description?.let {
                Text(text = it, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview
@Composable
fun FeaturedPagerItemPreview() {
    //FeaturedPagerItem(content = Featured.Broadcast(entity = BroadcastWithProgramAndEmployees.example))
}

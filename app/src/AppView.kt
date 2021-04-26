package com.denuafhaengige.duahandroid

import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat.getDrawableForDensity
import androidx.core.graphics.drawable.toBitmap

@Composable
fun App(viewModel: AppViewModel) {

    val playbackState by viewModel.playbackState.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Den Uafhængige")
                },
                backgroundColor = Color.Black,
                contentColor = Color.White,
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ActionButton(
                    playing = playbackState == AppViewModel.PlaybackState.PLAYING,
                    action = { viewModel.onPlaybackButtonTapped() },
                    loading = playbackState == AppViewModel.PlaybackState.LOADING
                )
                DescriptorText(
                    modifier = Modifier
                        .width(240.dp)
                        .padding(top = 30.dp)
                )
            }
        }
    )
}

@Composable
fun DescriptorText(modifier: Modifier) {

    val context = LocalContext.current
    val annotatedText = buildAnnotatedString {
        append("Læs mere om ")
        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
            append("Den Uafhængige")
        }
        append(" og bliv medlem her:\n")
        pushStringAnnotation(
            tag = "URL",
            annotation = "https://denuafhaengige.dk"
        )
        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("https://denuafhaengige.dk")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            )
            .firstOrNull()?.let { annotation ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(annotation.item)
                )
                context.startActivity(browserIntent)
            }
        },
        style = TextStyle(
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        ),
        modifier = modifier,
    )
}

@Composable
fun ActionButton(loading: Boolean = false, playing: Boolean = false, action: () -> Unit) {

    val icon = if (playing) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled

    IconButton(
        onClick = {
            action()
        },
        modifier = Modifier
            .size(width = 150.dp, height = 150.dp),
        enabled = !loading,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Play",
            Modifier
                .fillMaxSize()
        )
    }
}

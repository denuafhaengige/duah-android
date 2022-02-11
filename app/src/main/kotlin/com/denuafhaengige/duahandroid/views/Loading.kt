package com.denuafhaengige.duahandroid.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.model.KeyPath
import com.google.accompanist.insets.navigationBarsPadding
import com.denuafhaengige.duahandroid.AppState
import com.denuafhaengige.duahandroid.AppViewModel
import com.denuafhaengige.duahandroid.R
import java.text.DecimalFormat
import kotlin.math.round

@Composable
fun Loading(viewModel: AppViewModel) {

    val appState by viewModel.appState.observeAsState()
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.logo_animation_black))

    val lottieDynamicProperties = rememberLottieDynamicProperties(
        LottieDynamicProperty(
            property = LottieProperty.COLOR,
            keyPath = KeyPath("**"),
            value = MaterialTheme.colors.onBackground.hashCode(),
        ),
        LottieDynamicProperty(
            property = LottieProperty.STROKE_COLOR,
            keyPath = KeyPath("**"),
            value = MaterialTheme.colors.onBackground.hashCode(),
        ),
        LottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            keyPath = KeyPath("**"),
            value = SimpleColorFilter(MaterialTheme.colors.onBackground.hashCode()),
        )
    )

    val loadingState = appState as? AppState.Loading
    val descriptorFunc = loadingState?.descriptor
    val descriptor =
        descriptorFunc?.invoke(LocalContext.current) ?:
        stringResource(id = R.string.loading_descriptor_starting).replaceFirstChar { it.titlecase() }
    val progress = loadingState?.progress ?: 0F
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LottieAnimation(
            composition = lottieComposition,
            iterations = LottieConstants.IterateForever,
            dynamicProperties = lottieDynamicProperties,
            modifier = Modifier
                .size(180.dp)
                .align(alignment = Alignment.Center)
                .offset(y = (-30).dp)
        )
        if (appState is AppState.Loading) {
            Column(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 110.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    color = MaterialTheme.colors.onBackground
                )
                Text(
                    text = descriptor,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onBackground,
                )
            }
        }
    }

}

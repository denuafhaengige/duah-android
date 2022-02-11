package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.denuafhaengige.duahandroid.BroadcastsFilter
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.models.Program
import com.denuafhaengige.duahandroid.theming.DuahTheme
import com.denuafhaengige.duahandroid.util.LiveEntity

@Composable
fun DynamicProgramsContentRow(programs: List<LiveEntity<Program>>, navController: NavController) {

    val lazyRowState = rememberLazyListState()

    ProgramsContentRow {
        LazyRow(
            state = lazyRowState,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            items(items = programs) { liveProgram ->
                val observedProgram by liveProgram.liveEntity.observeAsState()
                val program = observedProgram ?: return@items
                ProgramsContentRowItem(program) {
                    navController.navigate(
                        route = NavigationRouteDest.BroadcastsList(
                            filter = BroadcastsFilter(
                                programIds = listOf(program.id)
                            )
                        ).destRoute
                    )
                }
            }
        }
    }
}

@Composable
fun ProgramsContentRow(content: @Composable () -> Unit) {

    ContentRow(
        title = casedStringResource(id = R.string.title_programs),
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(vertical = ContentDimensions.contentRowVerticalPadding)
            .fillMaxWidth(),
    ) {
        content()
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ProgramsContentRowItem(program: Program, action: () -> Unit = {}) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .width(ContentDimensions.squareBannerSize)
            .height(200.dp)
            .clickable { action() }
    ) {
        Image(
            painter = imagePainterForContent(content = program),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(ContentDimensions.squareBannerSize)
        )
        SmallTitleTextForContent(program)
    }
}

@Preview
@Composable
private fun ProgramsContentRowPreview() {
    DuahTheme {
        ProgramsContentRow {
            Row {
                ProgramsContentRowItem(program = Program.example)
                ProgramsContentRowItem(program = Program.example)
                ProgramsContentRowItem(program = Program.example)
                ProgramsContentRowItem(program = Program.example)
            }
        }
    }
}

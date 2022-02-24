package com.denuafhaengige.duahandroid.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.denuafhaengige.duahandroid.members.MemberAuthState
import com.denuafhaengige.duahandroid.members.MembersViewModel

@Composable
fun DynamicMemberButton(
    membersViewModel: MembersViewModel,
    modifier: Modifier = Modifier,
) {
    val authState by membersViewModel.authState.observeAsState()

    val context = LocalContext.current

    val state = when (authState) {
        null,
        is MemberAuthState.Loading -> MemberButtonState.LOADING
        is MemberAuthState.NotLoggedIn -> MemberButtonState.NOT_LOGGED_IN
        is MemberAuthState.LoggedIn -> MemberButtonState.LOGGED_IN
    }

    val action: () -> Unit = {
        when (state) {
            MemberButtonState.LOGGED_IN -> membersViewModel.showMemberOverview.value = true
            MemberButtonState.LOADING -> {}
            MemberButtonState.NOT_LOGGED_IN -> { membersViewModel.login(context) }
        }
    }

    MemberButton(state, action = action, modifier = modifier)
}

enum class MemberButtonState {
    LOADING,
    NOT_LOGGED_IN,
    LOGGED_IN,
}

@Composable
fun MemberButton(
    state: MemberButtonState,
    modifier: Modifier = Modifier,
    action: () -> Unit = {},
) {

    val colors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.onBackground,
        contentColor = MaterialTheme.colors.background,
        disabledBackgroundColor = MaterialTheme.colors.onBackground,
        disabledContentColor = MaterialTheme.colors.background,
    )

    val width = when (state) {
        MemberButtonState.LOADING,
        MemberButtonState.NOT_LOGGED_IN -> 30.dp
        MemberButtonState.LOGGED_IN -> 55.dp
    }

    Button(
        onClick = action,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(0.dp),
        colors = colors,
        enabled = true,
        modifier = modifier
            .size(width = width, height = 30.dp),
    ) {
        when (state) {
            MemberButtonState.LOADING -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxHeight(.68F)
                        .aspectRatio(1F)
                        .padding(4.dp),
                    strokeWidth = 2.dp,
                )
            }
            MemberButtonState.NOT_LOGGED_IN -> {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.Person),
                    contentDescription = "Decorative",
                    modifier = Modifier
                        .size(22.dp),
                )
            }
            MemberButtonState.LOGGED_IN -> {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.Person),
                    contentDescription = "Decorative",
                    modifier = Modifier
                        .size(22.dp),
                )
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.Done),
                    contentDescription = "Decorative",
                    modifier = Modifier
                        .size(22.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun MemberButtonPreview() {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .padding(30.dp)
    ) {
        MemberButton(state = MemberButtonState.LOGGED_IN)
        MemberButton(state = MemberButtonState.LOADING)
        MemberButton(state = MemberButtonState.NOT_LOGGED_IN)
    }

}

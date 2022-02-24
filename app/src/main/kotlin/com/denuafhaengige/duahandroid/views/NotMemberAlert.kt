package com.denuafhaengige.duahandroid.views

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.members.MembersViewModel

@Composable
fun NotMemberAlert(membersViewModel: MembersViewModel) {

    val showAlertDialog by membersViewModel.showNotLoggedInAlert.observeAsState(initial = false)
    val context = LocalContext.current

    NotMemberAlertTemplate(
        show = showAlertDialog,
        dismissAction = { membersViewModel.showNotLoggedInAlert.value = false },
        continueAction = {
            membersViewModel.showNotLoggedInAlert.value = false
            membersViewModel.login(context)
        },
    )
}

@Composable
private fun NotMemberAlertTemplate(
    show: Boolean = true,
    dismissAction: () -> Unit = {},
    continueAction: () -> Unit = {},
) {
    if (show) {
        AlertDialog(
            title = { Text(stringResource(id = R.string.not_member_alert_title)) },
            text = { Text(stringResource(id = R.string.not_member_alert_text)) },
            confirmButton = {
                TextButton(onClick = continueAction) {
                    Text(text = stringResource(id = R.string.not_member_alert_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = dismissAction) {
                    Text(text = stringResource(id = R.string.not_member_alert_cancel))
                }
            },
            onDismissRequest = dismissAction,
        )
    }
}

@Preview
@Composable
private fun NotMemberAlertPreview() {
    NotMemberAlertTemplate()
}

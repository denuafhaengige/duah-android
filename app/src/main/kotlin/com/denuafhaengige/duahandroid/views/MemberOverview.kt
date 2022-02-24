package com.denuafhaengige.duahandroid.views

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.denuafhaengige.duahandroid.R
import com.denuafhaengige.duahandroid.members.*
import com.denuafhaengige.duahandroid.theming.DarkGreenColor
import com.denuafhaengige.duahandroid.theming.RedColor
import com.denuafhaengige.duahandroid.util.DateFormat
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import java.util.*


@Composable
fun DynamicMemberOverview(
    settings: Settings,
    membersViewModel: MembersViewModel,
) {
    val showMemberOverview by membersViewModel.showMemberOverview.observeAsState(false)
    val authState by membersViewModel.authState.observeAsState(MemberAuthState.Loading)
    val subscriptionsLoading by membersViewModel.subscriptionsLoading.observeAsState(false)
    val subscriptions by membersViewModel.subscriptions.observeAsState(emptyList())
    val context = LocalContext.current

    MemberOverview(
        visible = showMemberOverview,
        hide = { membersViewModel.showMemberOverview.value = false },
        authState = authState,
        subscriptionsLoading = subscriptionsLoading,
        subscriptions = subscriptions.accessProviding,
        refreshSubscriptionsAction = { membersViewModel.refresh() },
        logOutAction = { membersViewModel.logout(context) },
        navigateToSubscribeAction = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, settings.subscribeUrl)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(context, intent, null)
            } catch (e: Throwable) {
                Log.debug("DynamicMemberOverview | navigateToSubscribeAction | error: $e")
            }
        },
    )
}

@Composable
fun MemberOverview(
    visible: Boolean = true,
    hide: () -> Unit = {},
    authState: MemberAuthState = MemberAuthState.NotLoggedIn,
    subscriptionsLoading: Boolean = false,
    subscriptions: List<MemberSubscription> = emptyList(),
    logOutAction: () -> Unit = {},
    refreshSubscriptionsAction: () -> Unit = {},
    navigateToSubscribeAction: () -> Unit = {},
) {

    BottomModal(
        visible = visible,
        hide = hide,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(id = R.string.member_overview_title),
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            GrayLine()
            when (authState) {
                is MemberAuthState.LoggedIn -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = stringResource(id = R.string.member_overview_text_logged_in_as))
                        Text(
                            text = authState.profile.email,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color.Black)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    GrayLine()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.member_overview_subtitle_memberships),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .width(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (subscriptionsLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp),
                                    color = Color.Black,
                                )
                            } else {
                                IconLabelButton(
                                    iconPainter = rememberVectorPainter(image = Icons.Default.Refresh),
                                    label = stringResource(id = R.string.member_overview_button_refresh),
                                    modifier = Modifier
                                        .height(30.dp),
                                    darkTheme = false,
                                    action = refreshSubscriptionsAction,
                                    enabled = !subscriptionsLoading,
                                )
                            }
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (subscriptions.isNotEmpty()) {
                                val locale = LocalContext.current.resources.configuration.locales[0]
                                for (subscription in subscriptions) {
                                    MemberSubscriptionCard(locale = locale, subscription = subscription)
                                }
                        } else {
                            Text(
                                text = stringResource(id = R.string.member_overview_no_memberships_text),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                )
                            )
                            IconLabelButton(
                                iconPainter = rememberVectorPainter(image = Icons.Default.Public),
                                label = "www.denuafhaengige.dk",
                                modifier = Modifier
                                    .height(30.dp),
                                darkTheme = false,
                                action = navigateToSubscribeAction,
                            )
                        }
                    }
                    GrayLine()
                    IconLabelButton(
                        iconPainter = rememberVectorPainter(image = Icons.Default.Logout),
                        label = stringResource(id = R.string.member_overview_button_log_out),
                        modifier = Modifier
                            .height(30.dp),
                        darkTheme = false,
                        action = logOutAction,
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MemberSubscriptionCard(locale: Locale, subscription: MemberSubscription) {
    Column(
        modifier = Modifier
            .width(250.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.LightGray.copy(.3F))
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val statusTextStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            when (subscription.status) {
                MemberSubscriptionStatus.ACTIVE ->
                    Text(
                        text = stringResource(id = R.string.member_overview_active),
                        style = statusTextStyle,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkGreenColor)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                else ->
                    Text(
                        text = stringResource(id = R.string.member_overview_cancelled),
                        style = statusTextStyle,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RedColor)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
            }
            Text(
                text = "id: ${subscription.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val renewsExpiresDate = subscription.nextChargeDateFormatted
            if (renewsExpiresDate != null) {
                val renewsExpiresDateString = DateFormat.dayMonthFormatted(
                    locale = locale,
                    date = renewsExpiresDate,
                )
                val prefix = when (subscription.chargeFrequency) {
                    MemberSubscriptionChargeFrequency.YEARLY ->
                        if (subscription.providesAccess) {
                            stringResource(id = R.string.member_overview_yearly_active_renews_on)
                        } else {
                            stringResource(id = R.string.member_overview_yearly_cancelled_expires_on)
                        }
                    else ->
                        if (subscription.providesAccess) {
                            stringResource(id = R.string.member_overview_monthly_active_renews_on)
                        } else {
                            stringResource(id = R.string.member_overview_monthly_cancelled_expires_on)
                        }
                }
                Text(
                    text = "$prefix\n$renewsExpiresDateString",
                    style = TextStyle(fontSize = 12.sp),
                )
            } else {
                Spacer(Modifier.width(0.dp))
            }
            val typeTextStyle = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
            when (subscription.type) {
                MemberSubscriptionType.APPLE ->
                    Text(
                        text = stringResource(id = R.string.member_overview_apple),
                        style = typeTextStyle,
                    )
                MemberSubscriptionType.MOBILE_PAY ->
                    Text(
                        text = stringResource(id = R.string.member_overview_mobile_pay),
                        style = typeTextStyle,
                    )
                else -> {}
            }
        }
    }
}

@Composable
private fun GrayLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(.5.dp)
            .background(Color.LightGray)
    )
}

@Preview
@Composable
fun MemberOverviewPreviewWithSubs() {
    MemberOverview(
        authState = MemberAuthState.LoggedIn(profile = MemberProfile("rasmus@hummelmose.dk")),
        subscriptions = listOf(
            MemberSubscription.example,
            MemberSubscription.example,
            MemberSubscription.example
        ),
        subscriptionsLoading = false,
    )
}

@Preview
@Composable
fun MemberOverviewPreviewNoSubs() {
    MemberOverview(
        authState = MemberAuthState.LoggedIn(profile = MemberProfile("rasmus@hummelmose.dk")),
        subscriptions = emptyList(),
        subscriptionsLoading = false,
    )
}

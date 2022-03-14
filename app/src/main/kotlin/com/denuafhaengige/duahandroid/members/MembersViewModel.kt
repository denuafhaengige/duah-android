package com.denuafhaengige.duahandroid.members

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.jwt.JWT
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.denuafhaengige.duahandroid.ActivityLifecycle
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.util.Settings
import com.denuafhaengige.duahandroid.util.memberSubscriptionListAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class MemberProfile(
    val email: String,
)

sealed class MemberAuthState {
    object Loading: MemberAuthState()
    object NotLoggedIn: MemberAuthState()
    data class LoggedIn(val profile: MemberProfile): MemberAuthState()
}

class MembersViewModel(settings: Settings) {

    // MARK: Static

    companion object {
        private const val scheme = "denuafhaengige"
        private const val authScope = "openid profile email offline_access"
        private const val audience = "https://duah.api"
        private const val subscriptionsPath = "subscriptions"
        private const val authorizationHeaderName = "Authorization"
        private const val bearerTokenPrefix = "Bearer "
    }

    // MARK: Props

    private val auth0 = Auth0(clientId = settings.auth0ClientId!!, domain = settings.auth0Domain!!)
    private val authentication = AuthenticationAPIClient(auth0)
    private val subscriptionsEndpoint = Uri.withAppendedPath(
        settings.membersEndpoint,
        subscriptionsPath
    )

    private val applicationContext: Context
        get() = Application.context.get()!!

    private val activityLifecycle: ActivityLifecycle
        get() = Application.activityLifecycle

    private val okHttpClient: OkHttpClient
        get() = Application.okHttpClient

    private val moshi: Moshi
        get() = Application.moshi

    private val prefs = MemberPrefs(applicationContext)
    private val scope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var credentials: Credentials? = null
    private var refreshToken: String? = null

    private val _authState = MutableLiveData<MemberAuthState>(MemberAuthState.Loading)
    val authState: LiveData<MemberAuthState> = _authState

    private val _subscriptionsLoading = MutableLiveData(false)
    val subscriptionsLoading: LiveData<Boolean> = _subscriptionsLoading

    private val _subscriptions = MutableLiveData<List<MemberSubscription>>(emptyList())
    val subscriptions: LiveData<List<MemberSubscription>> = _subscriptions

    val hasAccessProvidingSubscription = MediatorLiveData<Boolean>().apply {
        val refresh = { authState: MemberAuthState?, subscriptions: List<MemberSubscription>? ->
            val loggedIn = authState is MemberAuthState.LoggedIn
            val hasAccessProvidingSub = subscriptions?.hasAccessProvidingSubscription == true
            value = loggedIn && hasAccessProvidingSub
        }
        this.addSource(authState) {
            refresh(it, subscriptions.value)
        }
        this.addSource(subscriptions) {
            refresh(authState.value, it)
        }
    }

    val showMemberOverview = MutableLiveData(false)
    val showNotLoggedInAlert = MutableLiveData(false)

    // MARK: Init

    init {
        scope.launch {
            refreshIgnoringCache(ignoreCache = false)
            activityLifecycle.flow
                .filterIsInstance<ActivityLifecycle.Event.Resumed>()
                .collect {
                    Timber.d("MembersViewModel | activityLifecycle.flow.collect: $it")
                    refreshIgnoringCache(false)
                }
        }
    }

    // MARK: Public Functions

    fun login(context: Context) = scope.launch {
        try {
            withContext(mainScope.coroutineContext) {
                _authState.value = MemberAuthState.Loading
            }
            val credentials = suspendCoroutine<Credentials> { cont ->
                WebAuthProvider
                    .login(account = auth0)
                    .withScheme(scheme)
                    .withAudience(audience)
                    .withScope(authScope)
                    .start(context, object: Callback<Credentials, AuthenticationException> {
                        override fun onFailure(error: AuthenticationException) {
                            cont.resumeWithException(error)
                        }
                        override fun onSuccess(result: Credentials) {
                            cont.resume(result)
                        }
                    })
            }
            handleCredentials(credentials, saveToPrefs = true)
            refreshSubscriptionsByFetchingWithCredentials(credentials)
            withContext(mainScope.coroutineContext) {
                showMemberOverview.value = true
            }
            Timber.d("MembersViewModel | login | success")
        } catch (e: Throwable) {
            Timber.d("MembersViewModel | login | error: $e")
            clear()
        }
    }

    fun logout(context: Context) = scope.launch {
        try {
            withContext(mainScope.coroutineContext) {
                _authState.value = MemberAuthState.Loading
            }
            suspendCoroutine<Unit> { cont ->
                WebAuthProvider
                    .logout(account = auth0)
                    .withScheme(scheme)
                    .start(context, object: Callback<Void?, AuthenticationException> {
                        override fun onFailure(error: AuthenticationException) {
                            cont.resumeWithException(error)
                        }
                        override fun onSuccess(result: Void?) {
                            cont.resume(Unit)
                        }
                    })
            }
            withContext(mainScope.coroutineContext) {
                showMemberOverview.value = false
            }
            Timber.d("MembersViewModel | logout | success")
        } catch (e: Throwable) {
            Timber.d("MembersViewModel | logout | error: $e")
        } finally {
            clear()
        }
    }

    fun refresh() = scope.launch {
        refreshIgnoringCache(true)
    }

    fun handleRestrictedContentAccessAttempt() {
        if (authState.value !is MemberAuthState.LoggedIn) {
            showNotLoggedInAlert.value = true
            return
        }
        showMemberOverview.value = true
    }

    // MARK: Implementation

    private suspend fun refreshIgnoringCache(ignoreCache: Boolean) {
        Timber.d("MemberViewModel | refreshIgnoringCache: $ignoreCache")
        refreshAuthState()
        refreshSubscriptionsIgnoringCache(ignoreCache)
    }

    private suspend fun refreshAuthState() {
        val credentials = prefs.credentials
        val refreshToken = prefs.refreshToken
        if (credentials == null || refreshToken == null) {
            Timber.d("MemberViewModel | refreshAuthState | no credentials found, await user sanctioned login")
            clear()
            return
        }
        this.refreshToken = refreshToken
        handleCredentials(credentials, saveToPrefs = false)
        renewCredentialsIfAppropriate()
    }

    private suspend fun handleCredentials(credentials: Credentials, saveToPrefs: Boolean) {
        val idToken = JWT(credentials.idToken)
        val emailClaim = idToken.getClaim("email")
        val email = emailClaim.asString()
        if (email == null) {
            clear()
            return
        }
        if (saveToPrefs) {
            prefs.credentials = credentials
            credentials.refreshToken?.let { newRefreshToken ->
                prefs.refreshToken = newRefreshToken
            }
        }
        val profile = MemberProfile(email)
        this.credentials = credentials
        credentials.refreshToken?.let { newRefreshToken ->
            this.refreshToken = newRefreshToken
        }
        withContext(mainScope.coroutineContext) {
            _authState.value = MemberAuthState.LoggedIn(profile)
        }
    }

    private suspend fun renewCredentialsIfAppropriate() {
        try {
            val credentials = this.credentials ?: return
            val now = Date()
            if (credentials.expiresAt.time - now.time > 1000*60*60*12) {
                Timber.d("MemberViewModel | renewCredentialsIfAppropriate | more than 12 hours")
                Timber.d("MemberViewModel | renewCredentialsIfAppropriate | expiresAt: ${credentials.expiresAt}")
                return
            }
            val refreshToken = this.refreshToken
            if (refreshToken == null) {
                if (credentials.expiresAt < now) {
                    Timber.d("MemberViewModel | renewCredentialsIfAppropriate | credentials expired, no refreshToken found")
                    clear()
                }
                Timber.d("MemberViewModel | renewCredentialsIfAppropriate | no refreshToken found")
                return
            }
            val newCredentials = suspendCoroutine<Credentials> { cont ->
                authentication
                    .renewAuth(refreshToken)
                    .start(object: Callback<Credentials, AuthenticationException> {
                        override fun onFailure(error: AuthenticationException) {
                            cont.resumeWithException(error)
                        }
                        override fun onSuccess(result: Credentials) {
                            cont.resume(result)
                        }
                    })
            }
            Timber.d("MemberViewModel | renewCredentialsIfAppropriate | renewed")
            handleCredentials(newCredentials, saveToPrefs = true)
        } catch (e: Throwable) {
            Timber.d("MemberViewModel | renewCredentialsIfAppropriate | error: $e")
            clear()
        }
    }

    private suspend fun refreshSubscriptionsIgnoringCache(ignoreCache: Boolean) {
        try {
            val credentials = credentials
            if (credentials == null) {
                clear()
                return
            }
            val storedSubscriptions = prefs.subscriptions ?: emptyList()
            withContext(mainScope.coroutineContext) {
                _subscriptions.value = storedSubscriptions
            }
            if (!storedSubscriptions.hasAccessProvidingSubscription || ignoreCache) {
                refreshSubscriptionsByFetchingWithCredentials(credentials)
            }
        } catch (e: Throwable) {
            Timber.d("MemberViewModel | refreshSubscriptionsIgnoringCache | error: $e")
        }
    }

    private suspend fun refreshSubscriptionsByFetchingWithCredentials(credentials: Credentials) {
        try {
            Timber.d("MemberViewModel | refreshSubscriptionsByFetchingWithCredentials")
            withContext(mainScope.coroutineContext) {
                _subscriptionsLoading.value = true
            }
            val request = Request.Builder()
                .addHeader(
                    name = authorizationHeaderName,
                    value = bearerTokenPrefix + credentials.accessToken
                )
                .get()
                .url(subscriptionsEndpoint.toString())
                .build()
            val response = suspendCoroutine<Response> { continuation ->
                okHttpClient.newCall(request).enqueue(object: okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }
                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response)
                    }
                })
            }
            val body = response.body?.string()
            if (response.code != 200 || body == null) {
                Timber.d("MemberViewModel | refreshSubscriptionsByFetchingWithCredentials | response.code: ${response.code}, response.body: $body")
                clear()
                return
            }
            val loadedSubscriptions = moshi.memberSubscriptionListAdapter.fromJson(body)
            if (loadedSubscriptions == null) {
                Timber.d("MemberViewModel | refreshSubscriptionsByFetchingWithCredentials | failed deserializing body: $body")
                return
            }
            prefs.subscriptions = loadedSubscriptions
            withContext(mainScope.coroutineContext) {
                _subscriptions.value = loadedSubscriptions
            }
        } catch (e: Throwable) {
            Timber.d("MemberViewModel | refreshSubscriptionsByFetchingWithCredentials | error: $e")
        } finally {
            withContext(mainScope.coroutineContext) {
                _subscriptionsLoading.value = false
            }
        }
    }

    private suspend fun clear() {
        credentials = null
        refreshToken = null
        prefs.credentials = null
        prefs.refreshToken = null
        withContext(mainScope.coroutineContext) {
            _authState.value = MemberAuthState.NotLoggedIn
            _subscriptions.value = emptyList()
            _subscriptionsLoading.value = false
        }
    }
}

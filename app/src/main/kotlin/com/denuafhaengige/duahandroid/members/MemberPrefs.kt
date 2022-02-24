package com.denuafhaengige.duahandroid.members

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.auth0.android.result.Credentials
import com.denuafhaengige.duahandroid.Application
import com.squareup.moshi.Types

class MemberPrefs(applicationContext: Context) {

    companion object {
        private const val prefsFileName = "member_prefs"
        private const val keyCredentials = "credentials"
        private const val keyRefreshToken = "refresh_token"
        private const val keySubscriptions = "subscriptions"
    }

    var credentials: Credentials?
        get() {
            val string = prefs.getString(keyCredentials, null) ?: return null
            val adapter = moshi.adapter(Credentials::class.java) ?: return null
            return adapter.fromJson(string)
        }
        set(value) {
            if (value == null) {
                prefs.edit(commit = true) {
                    this.remove(keyCredentials)
                }
                return
            }
            val adapter = moshi.adapter(Credentials::class.java) ?: return
            val string = adapter.toJson(value)
            prefs.edit(commit = true) {
                this.putString(keyCredentials, string)
            }
        }

    var refreshToken: String?
        get() = prefs.getString(keyRefreshToken, null)
        set(value) = prefs.edit(commit = true) { putString(keyRefreshToken, value) }

    var subscriptions: List<MemberSubscription>?
        get() {
            val string = prefs.getString(keySubscriptions, null) ?: return null
            val type = Types.newParameterizedType(List::class.java, MemberSubscription::class.java)
            val adapter = moshi.adapter<List<MemberSubscription>>(type) ?: return null
            return adapter.fromJson(string)
        }
        set(value) {
            if (value == null) {
                prefs.edit(commit = true) {
                    this.remove(keySubscriptions)
                }
                return
            }
            val type = Types.newParameterizedType(List::class.java, MemberSubscription::class.java)
            val adapter = moshi.adapter<List<MemberSubscription>>(type) ?: return
            val string = adapter.toJson(value)
            prefs.edit(commit = true) {
                this.putString(keySubscriptions, string)
            }
        }

    private val prefs: SharedPreferences
    private val moshi = Application.moshi

    init {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
        prefs = EncryptedSharedPreferences.create(
            prefsFileName,
            mainKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}

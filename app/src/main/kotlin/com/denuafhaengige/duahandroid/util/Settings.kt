package com.denuafhaengige.duahandroid.util

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.g00fy2.versioncompare.Version
import kotlinx.coroutines.flow.first
import java.util.*
import android.content.pm.PackageInfo

import android.content.pm.PackageManager.GET_META_DATA
import android.net.Uri

class Settings(context: Context) {

    // MARK: Static

    private companion object {
        val LastFullContentLoadVersionKey = stringPreferencesKey("lastFullContentLoadVersion")
        val NewestChangeDetectedDuringLoadKey = longPreferencesKey("newestChangeDetectedDuringLoad")
        const val DataVersionResetThresholdKey = "data_version_reset_threshold"
        const val GraphEndpointKey = "graph_endpoint"
        const val RadioEndpointKey = "radio_endpoint"
        const val RadioEndpointOverrideKey = "radio_endpoint_override"
        const val StreamEndpointKey = "stream_endpoint"
        const val ShowHiddenContentKey = "show_hidden_content"
    }

    // MARK: Props

    val appVersionCode: Long
        get() = appPackageInfo.longVersionCode

    val appVersion: Version
        get() = Version(appPackageInfo.versionName)

    val dataVersionResetThreshold: Version
        get() = Version(appInfo.metaData.getString(DataVersionResetThresholdKey))

    val graphEndpoint: Uri
        get() = Uri.parse(appInfo.metaData.getString(GraphEndpointKey))

    val radioEndpoint: Uri
        get() = Uri.parse(appInfo.metaData.getString(RadioEndpointKey))

    val radioEndpointOverride: Uri?
        get() = appInfo.metaData.getString(RadioEndpointOverrideKey, null)?.let { Uri.parse(it) }

    val streamEndpoint: Uri
        get() = Uri.parse(appInfo.metaData.getString(StreamEndpointKey))

    val showHiddenContent: Boolean
        get() = appInfo.metaData.getString(ShowHiddenContentKey) == "true"

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
    private val dataStore: DataStore<Preferences> = context.dataStore
    private val appPackageInfo: PackageInfo by lazy {
        val manager = context.packageManager
        return@lazy manager.getPackageInfo(context.packageName, 0)
    }
    private val appInfo: ApplicationInfo by lazy {
        val manager = context.packageManager
        return@lazy manager.getApplicationInfo(context.packageName, GET_META_DATA)
    }

    // MARK: Public Interface

    suspend fun lastFullContentLoadVersion() = versionForKey(LastFullContentLoadVersionKey)
    suspend fun setLastFullContentLoadVersion(version: Version?) = setVersionForKey(version, LastFullContentLoadVersionKey)

    suspend fun newestChangeDetectedDuringLoad() = dateForKey(NewestChangeDetectedDuringLoadKey)
    suspend fun setNewestChangeDetectedDuringLoad(date: Date?) = setDateForKey(date, NewestChangeDetectedDuringLoadKey)

    // MARK: Implementation

    private suspend fun dateForKey(key: Preferences.Key<Long>): Date? {
        val data = dataStore.data.first()
        val timestamp = data[key] ?: return null
        return Date(timestamp)
    }

    private suspend fun setDateForKey(date: Date?, key: Preferences.Key<Long>) {
        dataStore.edit { preferences ->
            if (date == null) {
                preferences.remove(key)
                return@edit
            }
            val time = date.time
            preferences[key] = time
        }
    }

    private suspend fun versionForKey(key: Preferences.Key<String>): Version? {
        val data = dataStore.data.first()
        val string = data[key] ?: return null
        return try {
            Version(string, true)
        } catch (e: Throwable) {
            Log.debug("Settings | versionForKey | exception while parsing value: $string, exception: $e")
            null
        }
    }

    private suspend fun setVersionForKey(version: Version?, key: Preferences.Key<String>) {
        dataStore.edit { preferences ->
            if (version == null) {
                preferences.remove(key)
                return@edit
            }
            val string = version.stringValue()
            preferences[key] = string
        }
    }

}
package com.denuafhaengige.duahandroid
import android.content.Context
import android.content.res.Configuration
import com.denuafhaengige.duahandroid.content.ContentProvider
import com.squareup.moshi.Moshi
import com.denuafhaengige.duahandroid.util.MoshiFactory
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.time.Duration
import android.app.Application as AndroidApplication





class Application: AndroidApplication() {

    companion object {
        lateinit var context: WeakReference<Context>
        lateinit var activityLifecycle: ActivityLifecycle
        lateinit var okHttpClient: OkHttpClient
        lateinit var moshi: Moshi
        lateinit var contentProvider: ContentProvider
        lateinit var settings: Settings
    }

    // MARK: Application

    override fun onCreate() {
        Log.debug("Application | onCreate")
        context = WeakReference(this)
        activityLifecycle = ActivityLifecycle(this)
        settings = Settings(this)
        okHttpClient = OkHttpClient.Builder()
            .pingInterval(duration = Duration.ofSeconds(10))
            .build()
        moshi = MoshiFactory.moshi()
        contentProvider = ContentProvider(this)
        super.onCreate()
    }

    override fun onTerminate() {
        Log.debug("Application | onTerminate")
        super.onTerminate()
    }

    override fun onLowMemory() {
        Log.debug("Application | onLowMemory")
        super.onLowMemory()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.debug("Application | onConfigurationChanged | newConfig: $newConfig")
        super.onConfigurationChanged(newConfig)
    }

}

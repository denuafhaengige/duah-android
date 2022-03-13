package com.denuafhaengige.duahandroid
import android.content.Context
import android.content.res.Configuration
import com.denuafhaengige.duahandroid.content.ContentProvider
import com.squareup.moshi.Moshi
import com.denuafhaengige.duahandroid.util.MoshiFactory
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import okhttp3.OkHttpClient
import timber.log.Timber
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
        super.onCreate()

        Timber.d("Application | onCreate")
        context = WeakReference(this)
        activityLifecycle = ActivityLifecycle(this)
        settings = Settings(this)
        okHttpClient = OkHttpClient.Builder()
            .pingInterval(duration = Duration.ofSeconds(10))
            .build()
        moshi = MoshiFactory.moshi()
        contentProvider = ContentProvider(this)

        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true) // (Optional) Whether to show thread info or not. Default true
            .methodCount(1) // (Optional) How many method line to show. Default 2
            .methodOffset(5) // Set methodOffset to 5 in order to hide internal method calls
            .tag("") // To replace the default PRETTY_LOGGER tag with a dash (-).
            .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))


        Timber.plant(object : Timber.DebugTree() {

            override fun log(
                priority: Int, tag: String?, message: String, t: Throwable?
            ) {
                Logger.log(priority, "-$tag", message, t)
            }
        })

        // Usage
        Timber.d("onCreate: Inside Application!")
    }

    override fun onTerminate() {
        Timber.d("Application | onTerminate")
        super.onTerminate()
    }

    override fun onLowMemory() {
        Timber.d("Application | onLowMemory")
        super.onLowMemory()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Timber.d("Application | onConfigurationChanged | newConfig: $newConfig")
        super.onConfigurationChanged(newConfig)
    }

}

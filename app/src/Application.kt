package com.denuafhaengige.duahandroid
import android.content.Context
import android.content.res.Configuration
import com.denuafhaengige.duahandroid.content.ContentProvider
import com.squareup.moshi.Moshi
import com.denuafhaengige.duahandroid.graph.MoshiFactory
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.util.Settings
import java.lang.ref.WeakReference
import android.app.Application as AndroidApplication

class Application: AndroidApplication() {

    companion object {
        lateinit var context: WeakReference<Context>
        lateinit var moshi: Moshi
        lateinit var contentProvider: ContentProvider
        lateinit var settings: Settings
    }

    // MARK: Application

    override fun onCreate() {
        Log.debug("Application | onCreate")
        settings = Settings(this)
        moshi = MoshiFactory.moshi()
        contentProvider = ContentProvider(this)
        context = WeakReference(this)
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

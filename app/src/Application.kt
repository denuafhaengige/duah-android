package com.denuafhaengige.duahandroid
import com.squareup.moshi.Moshi
import com.denuafhaengige.duahandroid.graph.MoshiFactory
import android.app.Application as AndroidApplication

class Application: AndroidApplication() {

    companion object {
        lateinit var moshi: Moshi
    }

    // MARK: Application

    override fun onCreate() {
        moshi = MoshiFactory.moshi()
        super.onCreate()
    }

}

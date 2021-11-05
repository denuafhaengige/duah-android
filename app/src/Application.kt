package dk.denuafhaengige.android
import com.squareup.moshi.Moshi
import dk.denuafhaengige.android.graph.MoshiFactory
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

package com.denuafhaengige.duahandroid

import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.DisplayMetrics
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.MediaMetadata.*
import androidx.media2.common.UriMediaItem
import androidx.media2.session.MediaSession
import androidx.media2.session.MediaSessionService
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import java.util.*
import java.util.concurrent.Executors

class RadioService: MediaSessionService() {

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var sessionPlayerConnector: SessionPlayerConnector
    private lateinit var sessionCallback: MediaSession.SessionCallback
    private lateinit var mediaSession: MediaSession

    // MARK: Service

    override fun onCreate() {
        println("RadioService.kt onCreate()")
        super.onCreate()
        exoPlayer = SimpleExoPlayer.Builder(this)
            .build()
        sessionPlayerConnector = SessionPlayerConnector(exoPlayer)
        sessionCallback = SessionCallbackBuilder(this, sessionPlayerConnector)
            .setMediaItemProvider { _, _, mediaId ->
                val resource = resources.getDrawableForDensity(R.mipmap.ic_launcher_round, DisplayMetrics.DENSITY_XXXHIGH, null)
                val bitmap = resource.toBitmap()
                val metaData = MediaMetadata.Builder()
                    .putString(METADATA_KEY_MEDIA_ID, mediaId)
                    .putString(METADATA_KEY_ARTIST, "Den Uafhængige")
                    .putString(METADATA_KEY_DISPLAY_TITLE, "Live")
                    .putBitmap(METADATA_KEY_DISPLAY_ICON, bitmap)
                    .putBitmap(METADATA_KEY_ART, bitmap)
                    .build()
                UriMediaItem.Builder(Uri.parse(mediaId))
                    .setMetadata(metaData)
                    .build()
            }
            .build()
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }
        mediaSession = MediaSession.Builder(this, sessionPlayerConnector)
            .setSessionCallback(Executors.newSingleThreadExecutor(), sessionCallback)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
        println("RadioService.kt onCreate(): media session token: ${mediaSession.token}")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        println("RadioService.kt onDestroy()")
        exoPlayer.release()
        mediaSession.close()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

}

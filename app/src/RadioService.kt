package com.denuafhaengige.duahandroid

import android.net.Uri
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.MediaMetadata.METADATA_KEY_MEDIA_ID
import androidx.media2.common.UriMediaItem
import androidx.media2.session.MediaSession
import androidx.media2.session.MediaSessionService
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import java.util.concurrent.Executors

class RadioService: MediaSessionService() {

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var sessionPlayerConnector: SessionPlayerConnector
    private lateinit var sessionCallback: MediaSession.SessionCallback
    private lateinit var mediaSession: MediaSession

    // MARK: Service

    override fun onCreate() {
        super.onCreate()
        exoPlayer = SimpleExoPlayer.Builder(this)
            .build()
        sessionPlayerConnector = SessionPlayerConnector(exoPlayer)
        sessionCallback = SessionCallbackBuilder(this, sessionPlayerConnector)
            .setMediaItemProvider { _, _, mediaId ->
                val metaData = MediaMetadata.Builder()
                    .putString(METADATA_KEY_MEDIA_ID, mediaId)
                    .build()
                UriMediaItem.Builder(Uri.parse(mediaId))
                    .setMetadata(metaData)
                    .build()
            }
            .build()
        mediaSession = MediaSession.Builder(this, sessionPlayerConnector)
            .setSessionCallback(Executors.newSingleThreadExecutor(), sessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

}

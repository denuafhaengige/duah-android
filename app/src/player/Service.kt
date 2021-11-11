package com.denuafhaengige.duahandroid.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.denuafhaengige.duahandroid.Application
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.content.ContentProvider
import com.denuafhaengige.duahandroid.util.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.asListenableFuture
import java.lang.ref.WeakReference

@androidx.annotation.OptIn(UnstableApi::class)
class PlayerService: Player.Listener, MediaLibraryService() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var settings: Settings
    private val contentProvider
        get() = Application.contentProvider

    // MARK: Service

    override fun onCreate() {
        Log.debug("PlayerService | onCreate")
        super.onCreate()
        settings = Settings(context = applicationContext)
        wireMediaSession()
        Log.debug("PlayerService | onCreate | media session token: ${mediaSession.token}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.debug("PlayerService | onDestroy")
        exoPlayer.release()
        mediaSession.release()
    }

    // MARK: MediaLibraryService

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaSession
    }

    // MARK: Injected Callbacks

    @androidx.annotation.OptIn(UnstableApi::class)
    private val librarySessionCallback = object: MediaLibrarySession.MediaLibrarySessionCallback {
        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return scope.async {
                val mediaItem = mediaItemForMediaId(mediaId)
                    if (mediaItem != null) LibraryResult.ofItem(mediaItem, null)
                    else LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
            }
            .asListenableFuture()
        }
    }

    private val mediaItemFiller = object: MediaSession.MediaItemFiller {
        override fun fillInLocalConfiguration(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItem: MediaItem
        ): MediaItem {
            return runBlocking { mediaItemForMediaId(mediaItem.mediaId) }
                ?: return super.fillInLocalConfiguration(session, controller, mediaItem)
        }
    }

    // MARK: Logic

    private suspend fun mediaItemForMediaId(mediaId: String): MediaItem? {
        val mediaItemId = Playable.MediaItemId.forString(mediaId)
            ?: return null
        val playable = Playable.forMediaItemId(mediaItemId, contentProvider.contentStore)
            ?: return null
        return Media3Integration.mediaItemForPlayable(playable, settings)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun wireMediaSession() {
        val renderersFactory = DefaultRenderersFactory(this).setEnableAudioOffload(true)
        exoPlayer = ExoPlayer.Builder(this, renderersFactory)
            .setWakeMode(WAKE_MODE_NETWORK)
            .build()
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            } ?: return
        mediaSession = MediaLibrarySession.Builder(this, exoPlayer, librarySessionCallback)
            .setMediaItemFiller(mediaItemFiller)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

}

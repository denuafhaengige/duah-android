package com.denuafhaengige.duahandroid.player

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.*
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
import com.denuafhaengige.duahandroid.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.denuafhaengige.duahandroid.util.Log
import com.denuafhaengige.duahandroid.content.ContentProvider
import com.denuafhaengige.duahandroid.util.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.asListenableFuture
import timber.log.Timber
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
        Timber.d("PlayerService | onCreate")
        super.onCreate()
        settings = Settings(context = applicationContext)
        wireMediaSession()
        Timber.d("PlayerService | onCreate | media session token: ${mediaSession.token}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("PlayerService | onDestroy")
        exoPlayer.release()
        mediaSession.release()
    }

    // MARK: MediaLibraryService

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        Timber.d("PlayerService | onGetSession")
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
            Timber.d("PlayerService | onGetItem")
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
            Timber.d("PlayerService | fillInLocalConfiguration")
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
        // TODO: Find out why the fuck artwork doesn't work

        return Media3Integration.mediaItemForPlayable(playable, settings)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun wireMediaSession() {
        Timber.d("PlayerService | wireMediaSession")
        val renderersFactory = DefaultRenderersFactory(this)
            // TODO: Figure out why this causes playback not to work on some devices
            //.setEnableAudioOffload(true)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(USAGE_MEDIA)
            .setContentType(CONTENT_TYPE_SPEECH)
            .build()
        exoPlayer = ExoPlayer.Builder(this, renderersFactory)
            .setWakeMode(WAKE_MODE_NETWORK)
            .setAudioAttributes(audioAttributes, true)
            .build()
        val parentScreenIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent =
            TaskStackBuilder.create(this).run {
                addNextIntent(parentScreenIntent)
                val immutableFlag = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
                getPendingIntent(0, immutableFlag or PendingIntent.FLAG_UPDATE_CURRENT)
            } ?: return
        mediaSession = MediaLibrarySession.Builder(this, exoPlayer, librarySessionCallback)
            .setMediaItemFiller(mediaItemFiller)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

}

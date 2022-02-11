package com.denuafhaengige.duahandroid.player

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.denuafhaengige.duahandroid.Application
import com.denuafhaengige.duahandroid.util.Settings
import java.io.ByteArrayOutputStream

@androidx.annotation.OptIn(UnstableApi::class)
object Media3Integration {

    private suspend fun fetchBitmap(uri: Uri): Bitmap? {
        val context = Application.context.get() ?: return null
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(uri)
            .build()
        return when (val result = loader.execute(request)) {
            is SuccessResult -> {
                val drawable = result.drawable
                if (drawable is BitmapDrawable) {
                    return drawable.bitmap
                }
                return null
            }
            is ErrorResult -> null
        }
    }

    private suspend fun mediaMetaDataForPlayable(playable: Playable, fetchArtwork: Boolean = false): MediaMetadata {
        val metadataBuilder = MediaMetadata.Builder()
        when (playable) {
            is Playable.Broadcast -> metadataBuilder
                .setArtist(playable.broadcast.metaTitle)
                .setTitle(playable.broadcast.title)
                .setArtworkUri(playable.broadcast.squareImageUri)
            is Playable.Channel -> metadataBuilder
                .setArtist(playable.channel.metaTitle)
                .setTitle(playable.channel.title)
                .setArtworkUri(playable.channel.squareImageUri)
        }
        if (fetchArtwork) {
            val coverArtwork = playable.squareImageUri?.let {
                val bitmap = fetchBitmap(it) ?: return@let null
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG,10, stream)
                return@let stream.toByteArray()
            }
            metadataBuilder.setArtworkData(coverArtwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        return metadataBuilder.build()
    }

    suspend fun mediaItemForPlayable(playable: Playable, settings: Settings): MediaItem? {
        val metaData = mediaMetaDataForPlayable(playable)
        var stream = playable.preferredStreamWithSettings(settings) ?: return null
        return MediaItem.Builder()
            .setMediaId(playable.mediaItemId.stringValue)
            .setMediaMetadata(metaData)
            .setUri(stream.uri)
            .build()
    }

    fun stringForPlaybackState(state: Int): String = when (state) {
        Player.STATE_IDLE -> "STATE_IDLE"
        Player.STATE_BUFFERING -> "STATE_BUFFERING"
        Player.STATE_READY -> "STATE_READY"
        Player.STATE_ENDED -> "STATE_ENDED"
        else -> "UNKNOWN"
    }

    fun stringForMediaItemTransitionReason(code: Int): String = when (code) {
        Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> "MEDIA_ITEM_TRANSITION_REASON_AUTO"
        Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> "MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED"
        Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> "MEDIA_ITEM_TRANSITION_REASON_REPEAT"
        Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> "MEDIA_ITEM_TRANSITION_REASON_SEEK"
        else -> "UNKNOWN"
    }

    fun stringForTimeLineChangeReason(reason: Int): String = when (reason) {
        Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> "TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED"
        Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> "TIMELINE_CHANGE_REASON_SOURCE_UPDATE"
        else -> "UNKNOWN"
    }

//    fun stringForBufferingState(state: Int): String = when (state) {
//        BUFFERING_STATE_BUFFERING_AND_PLAYABLE -> "BUFFERING_STATE_BUFFERING_AND_PLAYABLE"
//        BUFFERING_STATE_BUFFERING_AND_STARVED -> "BUFFERING_STATE_BUFFERING_AND_STARVED"
//        BUFFERING_STATE_COMPLETE -> "BUFFERING_STATE_COMPLETE"
//        BUFFERING_STATE_UNKNOWN -> "BUFFERING_STATE_UNKNOWN"
//        else -> "UNKNOWN"
//    }
}

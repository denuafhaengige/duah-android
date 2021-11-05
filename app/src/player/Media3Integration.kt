package dk.denuafhaengige.android.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import dk.denuafhaengige.android.util.Settings

object Media3Integration {

    private fun mediaMetaDataForPlayable(playable: Playable): MediaMetadata {
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
        return metadataBuilder.build()
    }

    fun mediaItemForPlayable(playable: Playable, settings: Settings): MediaItem? {
        val stream = playable.preferredStreamWithSettings(settings) ?: return null
        val metaData = mediaMetaDataForPlayable(playable)
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

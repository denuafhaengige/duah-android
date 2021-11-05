package dk.denuafhaengige.android.player

import android.net.Uri

enum class StreamType {
    LIVE_AAC,
    HLS_VOD,
    HLS_EVENT,
    DIRECT_FILE,
}

data class Stream (
    val type: StreamType,
    val uri: Uri,
)

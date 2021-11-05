package com.denuafhaengige.duahandroid.graph

import android.net.Uri
import com.denuafhaengige.duahandroid.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.*
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.scarlet.ws.*
import dev.zacsweers.moshix.adapters.JsonString
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import java.util.*

interface GraphService {

    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocket.Event>

    @Send
    fun sendSubscription(message: GraphSubscriptionMessage)

    @Receive
    fun receiveSubscriptionResponse(): Flowable<GraphSubscriptionResponseMessage>

    @Receive
    fun receiveSubscriptionUpdate(): Flowable<GraphSubscriptionUpdateMessage>

    @Send
    fun sendRequest(message: GraphRequestMessage)

    @Receive
    fun receiveRequestResponse(): Flowable<GraphRequestResponseMessage>
}

class GraphServiceFactory(val moshi: Moshi) {

    fun createService(uri: Uri, lifecycle: Lifecycle): GraphService {
        val okHttpClient = OkHttpClient()
        val moshiMessageAdapterFactory = MoshiMessageAdapter.Factory(moshi)
        val rxJava2StreamAdapterFactory = RxJava2StreamAdapterFactory()
        val scarletInstance = Scarlet.Builder()
            .lifecycle(lifecycle)
            .webSocketFactory(okHttpClient.newWebSocketFactory(uri.toString()))
            .addMessageAdapterFactory(moshiMessageAdapterFactory)
            .addStreamAdapterFactory(rxJava2StreamAdapterFactory)
            .build()
        return scarletInstance.create()
    }
}

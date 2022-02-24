package com.denuafhaengige.duahandroid.graph

import android.net.Uri
import com.squareup.moshi.Moshi
import com.tinder.scarlet.*
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.scarlet.ws.*
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import java.time.Duration

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

class GraphServiceFactory(private val okHttpClient: OkHttpClient, private val moshi: Moshi) {

    fun createService(uri: Uri, lifecycle: Lifecycle): GraphService {
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

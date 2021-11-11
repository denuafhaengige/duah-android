package com.denuafhaengige.duahandroid.content

class ContentLoaderStateAdapter {

    companion object {
        fun ContentLoader.State.asServiceState(): ContentProvider.State {
            return when (this) {
                is ContentLoader.State.Paused ->
                    ContentProvider.State.Initial
                is ContentLoader.State.Starting ->
                    ContentProvider.State.WaitingForConnection
                is ContentLoader.State.WaitingForConnection ->
                    ContentProvider.State.WaitingForConnection
                is ContentLoader.State.Connected ->
                    ContentProvider.State.Loading(state = ContentProvider.LoadingState.Subscribing)
                is ContentLoader.State.Subscribing ->
                    ContentProvider.State.Loading(state = ContentProvider.LoadingState.Subscribing)
                is ContentLoader.State.Subscribed ->
                    ContentProvider.State.Loading(state = ContentProvider.LoadingState.Subscribing)
                is ContentLoader.State.Synchronizing -> {
                    when (this.syncState) {
                        is ContentLoader.SyncState.Initial ->
                            ContentProvider.State.Loading(state = ContentProvider.LoadingState.Subscribing)
                        is ContentLoader.SyncState.Send,
                        is ContentLoader.SyncState.AwaitResponse,
                        is ContentLoader.SyncState.HandleResponse -> {
                            val syncState = this.syncState as ContentLoader.SyncState.EntityTyped
                            ContentProvider.State.Loading(state = ContentProvider.LoadingState.Synchronizing(
                                entityType = syncState.entityType,
                                number = ContentLoader.entityTypeSyncSequence.indexOf(syncState.entityType) + 1,
                                of = ContentLoader.entityTypeSyncSequence.size
                            ))
                        }
                        is ContentLoader.SyncState.Done ->
                            ContentProvider.State.Loading(state = ContentProvider.LoadingState.Done)
                        else -> throw Throwable("Shut up")
                    }
                }
                is ContentLoader.State.Loaded ->
                    ContentProvider.State.PreparingContent
                else -> throw Throwable("Shut up")
            }
        }
    }


}

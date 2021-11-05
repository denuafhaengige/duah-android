package com.denuafhaengige.duahandroid.content

class ContentLoaderStateAdapter {

    companion object {
        fun ContentLoader.State.asServiceState(): ContentService.State {
            return when (this) {
                is ContentLoader.State.Paused ->
                    ContentService.State.Initial
                is ContentLoader.State.Starting ->
                    ContentService.State.WaitingForConnection
                is ContentLoader.State.WaitingForConnection ->
                    ContentService.State.WaitingForConnection
                is ContentLoader.State.Connected ->
                    ContentService.State.Loading(state = ContentService.LoadingState.Subscribing)
                is ContentLoader.State.Subscribing ->
                    ContentService.State.Loading(state = ContentService.LoadingState.Subscribing)
                is ContentLoader.State.Subscribed ->
                    ContentService.State.Loading(state = ContentService.LoadingState.Subscribing)
                is ContentLoader.State.Synchronizing -> {
                    when (this.syncState) {
                        is ContentLoader.SyncState.Initial ->
                            ContentService.State.Loading(state = ContentService.LoadingState.Subscribing)
                        is ContentLoader.SyncState.Send,
                        is ContentLoader.SyncState.AwaitResponse,
                        is ContentLoader.SyncState.HandleResponse -> {
                            val syncState = this.syncState as ContentLoader.SyncState.EntityTyped
                            ContentService.State.Loading(state = ContentService.LoadingState.Synchronizing(
                                entityType = syncState.entityType,
                                number = ContentLoader.entityTypeSyncSequence.indexOf(syncState.entityType) + 1,
                                of = ContentLoader.entityTypeSyncSequence.size
                            ))
                        }
                        is ContentLoader.SyncState.Done ->
                            ContentService.State.Loading(state = ContentService.LoadingState.Done)
                        else -> throw Throwable("Shut up")
                    }
                }
                is ContentLoader.State.Loaded ->
                    ContentService.State.PreparingContent
                else -> throw Throwable("Shut up")
            }
        }
    }


}

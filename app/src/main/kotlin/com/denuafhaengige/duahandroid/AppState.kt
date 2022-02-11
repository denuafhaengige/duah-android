package com.denuafhaengige.duahandroid

import android.content.Context
import com.denuafhaengige.duahandroid.content.ContentProvider
import com.denuafhaengige.duahandroid.models.EntityType

sealed class AppState {

    companion object {

        fun from(ContentProviderState: ContentProvider.State): AppState {
            return when (ContentProviderState) {
                is ContentProvider.State.Initial ->
                    initial
                is ContentProvider.State.WaitingForConnection ->
                    Loading(progress = 0.05F) { it.getString(R.string.loading_descriptor_connecting).replaceFirstChar { char -> char.titlecase() }}
                is ContentProvider.State.Loading -> {
                    when (ContentProviderState.state) {
                        is ContentProvider.LoadingState.Subscribing ->
                            Loading(progress = 0.1F) { it.getString(R.string.loading_descriptor_configuring).replaceFirstChar { char -> char.titlecase() }}
                        is ContentProvider.LoadingState.Synchronizing -> {
                            val progress = (ContentProviderState.state.number/ContentProviderState.state.of.toFloat())*0.85F+0.1F
                            val entityTypeResourceId = when (ContentProviderState.state.entityType) {
                                EntityType.BROADCAST -> R.plurals.entity_type_broadcast
                                EntityType.PROGRAM -> R.plurals.entity_type_program
                                EntityType.CHANNEL -> R.plurals.entity_type_channel
                                EntityType.EMPLOYEE -> R.plurals.entity_type_host_employee
                                EntityType.SETTING -> R.plurals.entity_type_setting
                                else -> throw Throwable("Unhandled entity type: ${ContentProviderState.state.entityType}")
                            }
                            Loading(progress) { context ->
                                context.getString(
                                    R.string.loading_descriptor_synchronizing,
                                    context.resources.getQuantityString(entityTypeResourceId, 2),
                                ).replaceFirstChar { char -> char.titlecase() }
                            }
                        }
                        is ContentProvider.LoadingState.Done ->
                            Loading(progress = 0.95F){ it.getString(R.string.loading_descriptor_preparing_content).replaceFirstChar { char -> char.titlecase() }}
                    }
                }
                is ContentProvider.State.PreparingContent ->
                    Loading(progress = 0.95F){ it.getString(R.string.loading_descriptor_preparing_content).replaceFirstChar { char -> char.titlecase() }}
                is ContentProvider.State.ReadyToServe ->
                    Ready
            }
        }

        val initial: Loading
            get() =
                Loading(progress = 0F) { it.getString(R.string.loading_descriptor_starting).replaceFirstChar { char -> char.titlecase() }}
    }

    data class Loading(val progress: Float, val descriptor: (context: Context) -> String): AppState()
    object Ready: AppState()
}

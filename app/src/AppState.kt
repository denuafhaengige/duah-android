package com.denuafhaengige.duahandroid

import android.content.Context
import com.denuafhaengige.duahandroid.content.ContentService
import com.denuafhaengige.duahandroid.models.EntityType

sealed class AppState {

    companion object {

        fun from(contentServiceState: ContentService.State): AppState {
            return when (contentServiceState) {
                is ContentService.State.Initial ->
                    initial
                is ContentService.State.WaitingForConnection ->
                    Loading(progress = 0.05F) { it.getString(R.string.loading_descriptor_connecting).replaceFirstChar { char -> char.titlecase() }}
                is ContentService.State.Loading -> {
                    when (contentServiceState.state) {
                        is ContentService.LoadingState.Subscribing ->
                            Loading(progress = 0.1F) { it.getString(R.string.loading_descriptor_configuring).replaceFirstChar { char -> char.titlecase() }}
                        is ContentService.LoadingState.Synchronizing -> {
                            val progress = (contentServiceState.state.number/contentServiceState.state.of.toFloat())*0.85F+0.1F
                            val entityTypeResourceId = when (contentServiceState.state.entityType) {
                                EntityType.BROADCAST -> R.plurals.entity_type_broadcast
                                EntityType.PROGRAM -> R.plurals.entity_type_program
                                EntityType.CHANNEL -> R.plurals.entity_type_channel
                                EntityType.EMPLOYEE -> R.plurals.entity_type_host_employee
                                EntityType.SETTING -> R.plurals.entity_type_setting
                                else -> throw Throwable("Unhandled entity type: ${contentServiceState.state.entityType}")
                            }
                            Loading(progress) { context ->
                                context.getString(
                                    R.string.loading_descriptor_synchronizing,
                                    context.resources.getQuantityString(entityTypeResourceId, 2),
                                ).replaceFirstChar { char -> char.titlecase() }
                            }
                        }
                        is ContentService.LoadingState.Done ->
                            Loading(progress = 0.95F){ it.getString(R.string.loading_descriptor_preparing_content).replaceFirstChar { char -> char.titlecase() }}
                        else -> throw Throwable("Shut up")
                    }
                }
                is ContentService.State.PreparingContent ->
                    Loading(progress = 0.95F){ it.getString(R.string.loading_descriptor_preparing_content).replaceFirstChar { char -> char.titlecase() }}
                is ContentService.State.ReadyToServe ->
                    Ready
                else -> throw Throwable("Shut up")
            }
        }

        val initial: Loading
            get() =
                Loading(progress = 0F) { it.getString(R.string.loading_descriptor_starting).replaceFirstChar { char -> char.titlecase() }}
    }

    data class Loading(val progress: Float, val descriptor: (context: Context) -> String): AppState()
    object Ready: AppState()
}

package com.denuafhaengige.duahandroid

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ActivityLifecycle(application: Application) {

    sealed class Event {
        data class Created(val activity: Activity, val bundle: Bundle?): Event()
        data class Started(val activity: Activity): Event()
        data class Resumed(val activity: Activity): Event()
        data class Paused(val activity: Activity): Event()
        data class Stopped(val activity: Activity): Event()
        data class SaveInstanceState(val activity: Activity, val bundle: Bundle): Event()
        data class Destroyed(val activity: Activity): Event()
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private val _flow = MutableSharedFlow<Event>()
    val flow = _flow.asSharedFlow()

    init {
        application.registerActivityLifecycleCallbacks(object: Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                scope.launch {
                    _flow.emit(Event.Created(activity = p0, bundle = p1))
                }
            }
            override fun onActivityStarted(p0: Activity) {
                scope.launch {
                    _flow.emit(Event.Started(activity = p0))
                }
            }
            override fun onActivityResumed(p0: Activity) {
                scope.launch {
                    _flow.emit(Event.Resumed(activity = p0))
                }
            }
            override fun onActivityPaused(p0: Activity) {
                scope.launch {
                    _flow.emit(Event.Paused(activity = p0))
                }
            }
            override fun onActivityStopped(p0: Activity) {
                scope.launch {
                    _flow.emit(Event.Stopped(activity = p0))
                }
            }
            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
                scope.launch {
                    _flow.emit(Event.SaveInstanceState(activity = p0, bundle = p1))
                }
            }
            override fun onActivityDestroyed(p0: Activity) {
                scope.launch {
                    _flow.emit(Event.Destroyed(activity = p0))
                }
            }
        })
    }
}

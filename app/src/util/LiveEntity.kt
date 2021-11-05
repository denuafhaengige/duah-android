package dk.denuafhaengige.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dk.denuafhaengige.android.content.EntityFlow
import dk.denuafhaengige.android.models.Entity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LiveEntity<T : Entity>(entityFlow: EntityFlow<T>) {

    private val _liveEntity = MutableLiveData<T?>(null)
    val liveEntity: LiveData<T?> = _liveEntity

    init {
        MainScope().launch {
            entityFlow.flow.collect { _liveEntity.value = it }
        }
    }

}
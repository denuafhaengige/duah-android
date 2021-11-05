package dk.denuafhaengige.android.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dk.denuafhaengige.android.content.Featured
import dk.denuafhaengige.android.content.FeaturedFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LiveFeatured(featuredFlow: FeaturedFlow) {

    private val _liveFeatured = MutableLiveData<Featured?>(null)
    val liveFeatured: LiveData<Featured?> = _liveFeatured

    init {
        MainScope().launch {
            featuredFlow.flow.collect { _liveFeatured.value = it }
        }
    }

}
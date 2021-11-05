package com.denuafhaengige.duahandroid.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denuafhaengige.duahandroid.content.Featured
import com.denuafhaengige.duahandroid.content.FeaturedFlow
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
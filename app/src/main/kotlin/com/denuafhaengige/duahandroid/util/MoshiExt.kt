package com.denuafhaengige.duahandroid.util

import com.denuafhaengige.duahandroid.members.MemberSubscription
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

val Moshi.memberSubscriptionListAdapter: JsonAdapter<List<MemberSubscription>>
    get() {
        val type = Types.newParameterizedType(List::class.java, MemberSubscription::class.java)
        return adapter(type)
    }

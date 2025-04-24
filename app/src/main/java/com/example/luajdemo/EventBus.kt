package com.example.luajdemo

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "EventBus"

class EventBus {

    private val flowMap = ConcurrentHashMap<String, MutableSharedFlow<Any?>>()

    fun register(tag: String): SharedFlow<Any?> {
        val mutableSharedFlow = MutableSharedFlow<Any?>(
            replay = 1,
            extraBufferCapacity = 1
        )
        val flow = mutableSharedFlow.asSharedFlow()
        flowMap[tag] = mutableSharedFlow
        Log.d(TAG, "register: $tag")
        return flow
    }

    fun post(tag: String, params: Any) {
        Log.d(TAG, "post: $tag, par=${params}")
        flowMap[tag]?.tryEmit(params)
    }

}
package com.rafaelgcpp.mlacontroller.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun <T> LifecycleOwner.observe(
    liveData: LiveData<T>,
    observer: (value: T) -> Unit
) {
    liveData.observe(this) {
        observer.invoke(it)
    }
}
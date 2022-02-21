package com.rafaelgcpp.mlacontroller.util

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

internal fun Context.showAlert(message: CharSequence) {
    MaterialAlertDialogBuilder(this)
        .setMessage(message)
        .show()
}

// Creates a child job to the main job of a scope. This child job does the BLE scanning
internal fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

// Stops all children jobs for a scope
internal fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)
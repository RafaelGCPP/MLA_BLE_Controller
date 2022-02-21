package com.rafaelgcpp.mlacontroller

import android.app.Application
import timber.log.Timber

class MlaControllerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
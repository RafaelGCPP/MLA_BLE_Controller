package com.rafaelgcpp.mlacontroller

import android.os.Bundle
import androidx.activity.viewModels
import com.rafaelgcpp.mlacontroller.bluetooth.BtAppCompatActivity
import com.rafaelgcpp.mlacontroller.ui.device.DeviceFragment
import com.rafaelgcpp.mlacontroller.viewmodel.MainViewModel
import timber.log.Timber


class MainActivity : BtAppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DeviceFragment.newInstance())
                .commitNow()
        }

        setInitBluetoothHandler { btManager ->
            Timber.i("btManager ==> $btManager")
        }
    }
}



package com.rafaelgcpp.mlacontroller

import android.os.Bundle
import android.util.Log
import com.rafaelgcpp.mlacontroller.ui.main.MainFragment

class MainActivity : BtAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }



        setInitBluetoothHandler { btManager ->
            Log.d("Test", "initBluetoothHandler called")
            Log.d("Test", btManager.toString())
        }
    }


}

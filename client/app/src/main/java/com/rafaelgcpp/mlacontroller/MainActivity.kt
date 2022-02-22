package com.rafaelgcpp.mlacontroller

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.rafaelgcpp.mlacontroller.bluetooth.BtAppCompatActivity
import timber.log.Timber


class MainActivity : BtAppCompatActivity(R.layout.main_activity) {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.navController


        val appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.deviceFragment,
                R.id.mainFragment)
            .build()

        // Set up the action bar for use with the NavController
        setupActionBarWithNavController(navController,appBarConfiguration)

        setInitBluetoothHandler { btManager ->
            Timber.i("btManager ==> $btManager")
        }
    }
}



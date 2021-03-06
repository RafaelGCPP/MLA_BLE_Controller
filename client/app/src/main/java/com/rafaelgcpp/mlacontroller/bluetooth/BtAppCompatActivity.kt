@file:Suppress("unused")

package com.rafaelgcpp.mlacontroller.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber


open class BtAppCompatActivity (@LayoutRes layoutId:Int): AppCompatActivity(layoutId) {

    private lateinit var _bluetoothManager: BluetoothManager


    private val bluetoothManager: BluetoothManager
        get() = _bluetoothManager

    val bluetoothAdapter: BluetoothAdapter
        get() = bluetoothManager.adapter

    private val isBluetoothEnabled: Boolean
        get() {
            val bluetoothAdapter = _bluetoothManager.adapter ?: return false
            return bluetoothAdapter.isEnabled
        }


    private val enableBluetoothRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // Bluetooth has been enabled
                checkPermissions()
            } else {
                // Bluetooth has not been enabled, try again
                askToEnableBluetooth()
            }
        }

    @Synchronized
    private fun askToEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothRequest.launch(enableBtIntent)
    }

    @Synchronized
    private fun checkPermissions() {
        val missingPermissions = getMissingPermissions(requiredPermissions)
        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST)
        } else {
            checkIfLocationIsNeeded()
        }
    }

    private val requiredPermissions: Array<String>
        get() {
            val targetSdkVersion = applicationInfo.targetSdkVersion
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            } else arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

    private fun getMissingPermissions(requiredPermissions: Array<String>): Array<String> {
        val missingPermissions: MutableList<String> = ArrayList()
        for (requiredPermission in requiredPermissions) {
            if (applicationContext.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(requiredPermission)
            }
        }
        return missingPermissions.toTypedArray()
    }

    private fun checkIfLocationIsNeeded() {
        val targetSdkVersion = applicationInfo.targetSdkVersion
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S) {
            // Check if Location services are on because they are required to make scanning work for SDK < 31
            if (checkLocationServices()) {
                initBluetoothHandler()
            }
        } else {
            initBluetoothHandler()
        }
    }

    private lateinit var initBluetoothHandler: () -> Unit

    fun setInitBluetoothHandler(handler: (btManager: BluetoothManager) -> Unit) {
        initBluetoothHandler = { handler(bluetoothManager) }
    }

    private fun checkLocationServices(): Boolean {
        return if (!areLocationServicesEnabled()) {
            MaterialAlertDialogBuilder(this@BtAppCompatActivity)
                .setTitle("Location services are not enabled")
                .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                .setPositiveButton("Enable") { dialogInterface, _ ->
                    dialogInterface.cancel()
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel()
                }
                .create()
                .show()
            false
        } else {
            true
        }
    }

    private fun areLocationServicesEnabled(): Boolean {
        val locationManager =
            applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            isGpsEnabled || isNetworkEnabled
        }
    }


    @get:Synchronized
    @set:Synchronized
    private var isDialogShowing: Boolean = false

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if all permission were granted
        var allGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
        }
        if (allGranted) {
            checkIfLocationIsNeeded()
        } else {
            if (!isDialogShowing) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Location permission is required for scanning Bluetooth peripherals")
                    .setMessage("Please grant permissions")
                    .setPositiveButton("Retry") { dialogInterface, _ ->
                        dialogInterface.cancel()
                        checkPermissions()
                        isDialogShowing = false
                    }
                    .create()
                    .show()
                isDialogShowing = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bluetoothManager =
            applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager

        registerReceiver(mReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }


    override fun onResume() {
        super.onResume()
        if (_bluetoothManager.adapter != null) {
            if (!isBluetoothEnabled) {
                askToEnableBluetooth()
            } else {
                checkPermissions()
            }
        } else {
            Timber.e("This device has no Bluetooth hardware")
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val ACCESS_LOCATION_REQUEST = 2
    }

    open fun onBluetoothState(isEnabled: Boolean) {
        if ((!isEnabled) && (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)))
            Toast.makeText(
                this,
                "Bluetooth must be enabled for this application!",
                Toast.LENGTH_LONG
            ).show()
        askToEnableBluetooth()
    }


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // It means the user has changed his bluetooth state.
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                if (bluetoothAdapter.state == BluetoothAdapter.STATE_ON) {
                    onBluetoothState(true)
                    return
                }
                if (bluetoothAdapter.state == BluetoothAdapter.STATE_OFF) {
                    onBluetoothState(false)
                    return
                }
            }
        }
    }
}
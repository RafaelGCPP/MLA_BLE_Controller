package com.rafaelgcpp.mlacontroller.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.rafaelgcpp.mlacontroller.util.cancelChildren
import com.rafaelgcpp.mlacontroller.util.childScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

sealed class ScanStatus {
    object Stopped : ScanStatus()
    object Started : ScanStatus()
    data class Failed(val message: CharSequence) : ScanStatus()
}

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

class MainViewModel : ViewModel() {

    private val scanner = Scanner {
        filters = null
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Events
            format = Logging.Format.Multiline
        }
    }

    private val scanScope = viewModelScope.childScope()
    private val found = hashMapOf<String, Advertisement>()

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Stopped)
    val scanStatus = _scanStatus.asStateFlow()

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
    val advertisements = _advertisements.asStateFlow()


    fun startScan() {

        if (_scanStatus.value == ScanStatus.Started) return // Scan already in progress.
        _scanStatus.value = ScanStatus.Started

        scanScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner
                    .advertisements
                    .catch { cause ->
                        _scanStatus.value =
                            ScanStatus.Failed(cause.message ?: "Unknown error")
                    }
                    .onCompletion { cause ->
                        if ((cause is TimeoutCancellationException) || (cause is CancellationException))
                            _scanStatus.value = ScanStatus.Stopped
                        else
                            _scanStatus.value =
                                ScanStatus.Failed(cause?.message ?: "Unknown error")
                    }
                    .filter { /*it.isSensorTag*/  true }
                    .collect { advertisement ->
                        found[advertisement.address] = advertisement
                        _advertisements.value = found.values.toList()
                    }
            }
        }
    }

    fun stopScan() {
        scanScope.cancelChildren()
    }
}

// Verifies the device name
private val Advertisement.isSensorTag
    get() = name?.startsWith("SensorTag") == true ||
            name?.startsWith("CC2650 SensorTag") == true


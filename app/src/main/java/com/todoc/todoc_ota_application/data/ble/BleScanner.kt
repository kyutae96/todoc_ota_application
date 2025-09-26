package com.todoc.todoc_ota_application.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

class BleScanner(context: Context) {
    private val TAG = this.javaClass.simpleName
    var bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    var adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner get() = adapter?.bluetoothLeScanner!!

    private val map = ConcurrentHashMap<String, ScanResult>()
    private val _devices = MutableStateFlow<List<ScanResult>>(emptyList())
    val devices: StateFlow<List<ScanResult>> = _devices

    private val _scanning = MutableStateFlow<ScanningState>(ScanningState.NotScanning)
    val scanning: StateFlow<ScanningState> = _scanning


    private val callback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val name = result?.device?.name ?: return
//            Log.e(TAG, "├ ScanResult Name : ${name}")

            if (!name.startsWith("TD_")) return
            Log.e(TAG, "├ ScanResult Name : ${name}")
//            val serviceBytes = result?.scanRecord?.serviceData?.get(SERVICE_DATA_UUID) ?: return
//            val serviceString = String(serviceBytes)


            result.device?.address?.let { addr ->
                map[addr] = result
                _devices.value = map.values.sortedByDescending { it.device.name }
            }

        }
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { onScanResult(0, it) }
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        Log.d(TAG,"Start Scan")
        if (!adapter!!.isEnabled) return
        _scanning.value = ScanningState.Scanning
        Log.d(TAG,"adapter is Enable")
        map.clear()
        _devices.value = emptyList()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)  // 빠르게 광고 수신
            .build()
        scanner.startScan(null, scanSettings, callback)
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        _scanning.value = ScanningState.NotScanning
        runCatching { scanner.stopScan(callback) }
    }
}
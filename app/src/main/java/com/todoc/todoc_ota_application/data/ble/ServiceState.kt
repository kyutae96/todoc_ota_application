package com.todoc.todoc_ota_application.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.todoc.todoc_ota_application.R
import com.todoc.todoc_ota_application.core.model.AckResult

import com.todoc.todoc_ota_application.feature.permission.PermissionHelper
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

sealed class ServiceState {
    data object Idle : ServiceState()
    data object Discovering : ServiceState()
    data class Ready(val mtu: Int) : ServiceState() // mtu는 참고용(기기에서 올려줬다면 반영, 아니면 기본 20)
    data class Failed(val status: Int) : ServiceState()
}
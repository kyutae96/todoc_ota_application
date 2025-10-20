package com.todoc.todoc_ota_application.data.ble


import android.os.ParcelUuid
import java.util.UUID

object OtaGattSpec {
    val SERVICE_OTA: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")// service
    val CHAR_TX: UUID     = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e") // App → Device
    val CHAR_RX: UUID     = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e") // Device → App (Notify/Indicate)
    val CCCD: UUID        = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") // 표준 CCCD
    val SERVICE_DATA_UUID = ParcelUuid(UUID.fromString("00004944-0000-1000-8000-00805F9B34FB"))


    val QUALCOMM_UUID = UUID.fromString("00001100-d102-11e1-9b23-00025b00a5a5")// service
    val GaiaCommand_UUID = UUID.fromString("00001101-d102-11e1-9b23-00025b00a5a5")// service
    val GaiaResponse_UUID = UUID.fromString("00001102-d102-11e1-9b23-00025b00a5a5")// service
    val GaiaData_UUID = UUID.fromString("00001103-d102-11e1-9b23-00025b00a5a5")


    val BATTERY_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb") // service
    val BATTERY_CHARACTERISTIC = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb") // characteristic
}
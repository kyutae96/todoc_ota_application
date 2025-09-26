package com.todoc.todoc_ota_application.data.ota

import android.content.Context
import android.util.Log
import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.CoroutineScope

object CreatePacketOta {
    private val TAG = this.javaClass.simpleName
/*

    fun createPacketCmd1(data: QRDecodedUserInfo, slot: Int): ByteArray {
        val packet = mutableListOf<Byte>()
        packet.add(HEADER_WRITE_ISD_ID_AND_USER)                // 고정
        packet.add(0x01)                // 데이터 인덱스
        packet.add(slot.toByte())       // 슬롯 번호 (1~4)

//        val deviceId = data.mapIdentifier.toByteArray(Charsets.UTF_8).padToSize(4)
//        packet.addAll(deviceId)
//        packet.add(data.userModel.internalSerial.toByte())

        val internalSerialBytes = encodeInternalIdToBytes(data.userModel.internalSerial)        //여기 확인 필요

        val str = data.userModel.internalSerial
        val bytes = str.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        Log.e(TAG, "internalSerialBytes: ${printLogBytesToString(internalSerialBytes)}")
        Log.e(TAG, "bytes: ${printLogBytesToString(bytes)}")

//        packet.addAll(internalSerialBytes.toList())  //내부기 ID -> 4byte
        packet.addAll(bytes.toList())  //내부기 ID -> 4byte

        packet.add(data.surgeryPositon.toByte())  // 1: 좌, 2: 우


        val fullUserInitial = getPaddedUserInitial(data.userModel.userInitial)
        val part1 = fullUserInitial.subList(0, 12)
        packet.addAll(part1)


//        val nameBytes = data.userModel.userInitial.toByteArray(Charsets.UTF_8).padToSize(12)
//        packet.addAll(nameBytes)

        Log.e(TAG, "createPacketCmd1 : ${printLogBytesToString(packet.toByteArray())}")
        return packet.toByteArray()
    }
*/

}
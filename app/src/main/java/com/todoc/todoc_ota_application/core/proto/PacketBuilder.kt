package com.todoc.todoc_ota_application.core.proto

import android.util.Log
import com.todoc.todoc_ota_application.data.ble.PacketInfo.GAIA_HEADER
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_START_COMMAND
import kotlin.experimental.and

object PacketBuilder {
    private val TAG = this.javaClass.simpleName

    fun c2Select(slot: Int) =
        byteArrayOf(0xC2.toByte(), 0x02, slot.toByte())

    private fun i16(v: Int) = byteArrayOf(
        ((v ushr 8) and 0xFF).toByte(),
        (v and 0xFF).toByte()
    )

    private fun i24(v: Int) = byteArrayOf(
        ((v ushr 16) and 0xFF).toByte(),
        ((v ushr 8) and 0xFF).toByte(),
        (v and 0xFF).toByte()
    )

    private fun i32(v: Long) = byteArrayOf(
        ((v ushr 24) and 0xFF).toByte(),
        ((v ushr 16) and 0xFF).toByte(),
        ((v ushr 8) and 0xFF).toByte(),
        (v and 0xFF).toByte()
    )

    /** C3 Index=0 헤더 생성 */
    fun c3WriteHeader(
        slotNum: Int,
        fileNum: Int,
        fileLength: Long,
        endIndexNum: Long,
        endIndexByte: Int
    ): ByteArray {
        val out = ByteArray(18)
        var i = 0
        out[i++] = 0xC3.toByte()           // Header
        i24(0).copyInto(out, i); i += 3    // DataIndex = 0x000000
        out[i++] = (slotNum and 0xFF).toByte()
        i16(fileNum).copyInto(out, i); i += 2
        out[i++] = 0x01                    // Option = Write
        i32(fileLength).copyInto(out, i); i += 4
        i32(endIndexNum).copyInto(out, i); i += 4
        i16(endIndexByte).copyInto(out, i) // 마지막 2B
        return out
    }

    fun c3WriteDataPayload(index: Int, payload: ByteArray): ByteArray {
        require(index in 1..0xFFFFFF) { "index must be 1..16777215" }
        val out = ByteArray(3 + payload.size)
        out[0] = ((index ushr 16) and 0xFF).toByte()
        out[1] = ((index ushr 8) and 0xFF).toByte()
        out[2] = (index and 0xFF).toByte()
        System.arraycopy(payload, 0, out, 3, payload.size)
        return out
    }

    /** C3 Index=0 헤더 생성 */
    fun c3WriteWithOutHeader(
        slotNum: Int,
        fileNum: Int,
        fileLength: Long,
        endIndexNum: Long,
        endIndexByte: Int
    ): ByteArray {
        val out = ByteArray(17)
        var i = 0
//        out[i++] = 0xC3.toByte()           // Header
        i24(0).copyInto(out, i); i += 3    // DataIndex = 0x000000
        out[i++] = (slotNum and 0xFF).toByte()
        i16(fileNum).copyInto(out, i); i += 2
        out[i++] = 0x01                    // Option = Write
        i32(fileLength).copyInto(out, i); i += 4
        i32(endIndexNum).copyInto(out, i); i += 4
        i16(endIndexByte).copyInto(out, i) // 마지막 2B
        return out
    }

    fun c3WriteData(index: Int, payload: ByteArray): ByteArray {
        val out = ByteArray(4 + payload.size)
        out[0] = 0xC3.toByte()
        out[1] = ((index ushr 16) and 0xFF).toByte()
        out[2] = ((index ushr 8) and 0xFF).toByte()
        out[3] = (index and 0xFF).toByte()
        System.arraycopy(payload, 0, out, 4, payload.size)
        return out
    }

    fun packetMaker(header: Byte, payload: ByteArray?, maxLen: Int): ByteArray {


        val gaiaHeader = GAIA_HEADER
        val commandHeader = gaiaHeader + header
        val fullPacket = if (payload != null) commandHeader + payload else commandHeader

        Log.d(TAG, "새롭게 생성한 패킷 : ${printLogBytesToString(fullPacket)}")
        return fullPacket
    }

    fun printLogBytesToString(bytes: ByteArray): String {
        val sb = StringBuilder()
        sb.append("0x")

        for (b in bytes) {
            sb.append(String.format("%02X ", b and 0xFF.toByte()))
        }

        return sb.toString()
    }

    fun byteExtractor(b: Byte): Byte {
        return (b and 0xFF.toByte()).toByte()
    }
    fun Byte.toHex(): String = this.toUByte().toString(16).uppercase().padStart(2, '0')

}
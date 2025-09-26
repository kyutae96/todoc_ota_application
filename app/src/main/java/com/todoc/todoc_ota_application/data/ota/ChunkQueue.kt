package com.todoc.todoc_ota_application.data.ota

import android.util.Log
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.printLogBytesToString
import com.todoc.todoc_ota_application.domain.ota.OtaLocalCollector.intTo3Bytes
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.math.min

class ChunkQueue {
    private val TAG = this.javaClass.simpleName

    private val map = ConcurrentSkipListMap<Int, ByteArray>()

    fun enqueue(index: Int, data: ByteArray) {
        map[index] = data
    }

    /** 가장 작은 인덱스 하나 반환+제거 (전송 루프에서 사용) */
    fun poll(): Pair<Int, ByteArray>? =
        map.pollFirstEntry()?.let { it.key to it.value }

    /** 가장 작은 인덱스 보기(제거 X) */
    fun peekFirst(): Pair<Int, ByteArray>? =
        map.firstEntry()?.let { it.key to it.value }

    fun clear() = map.clear()
    fun size(): Int = map.size

    /** 스냅샷(소비 안 함) */
    fun snapshot(max: Int = 20): List<Pair<Int, ByteArray>> {
        val out = ArrayList<Pair<Int, ByteArray>>(min(max, map.size))
        var cnt = 0
        for ((k, v) in map.entries) {
            out += k to v
            if (++cnt >= max) break
        }
        return out
    }

    /** 디버그 로그: 큐의 앞부분을 헥사로 덤프 */
    fun dumpForDebug(tag: String = "OTA-QUEUE") {
        Log.d(tag, "Queue size=${map.size}")
        for ((idx, payload) in map.entries) {
            Log.d(tag, "  idx=${printLogBytesToString(intTo3Bytes(idx))} len=${payload.size} head=${printLogBytesToString(payload)}")
//                if (++cnt >= max) break
            /*
            *     sendPacket[1] = (byte) ((mOta.commDataIndex >> 16) & 0xFF); // Data index (MSB to LSB)
                sendPacket[2] = (byte) ((mOta.commDataIndex >> 8) & 0xFF);
                sendPacket[3] = (byte) (mOta.commDataIndex & 0xFF);
            * */

        }
    }

    /** lastSuccess 이후(>= startIndex) 대기 중 청크 삭제 (재전송 롤백용) */
    fun removeFromIndexInclusive(startIndex: Int): Int {
        val tail = map.tailMap(startIndex, true)
        val n = tail.size
        tail.clear()
        return n
    }
}





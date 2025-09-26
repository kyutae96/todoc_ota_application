package com.todoc.todoc_ota_application.domain.ota


import com.todoc.todoc_ota_application.core.model.CollectProgress
import com.todoc.todoc_ota_application.core.model.OtaCommand
import com.todoc.todoc_ota_application.core.model.OtaCommandType
import com.todoc.todoc_ota_application.core.model.OtaProgress
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_START_COMMAND
import com.todoc.todoc_ota_application.data.ota.ChunkQueue
import com.todoc.todoc_ota_application.data.ota.OtaCommandQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

/**
 * 풀다운로드된 로컬 파일을 BLE 전송 청크 크기(chunkSize)로 잘라 Queue에 넣는다.
 * - Index는 1부터 시작, 마지막 패킷만 EndIndexByte 길이가 될 수 있음.
 * - 진행률 콜백으로 (collected/total) 갱신.
 */
object OtaLocalCollector {

    suspend fun collectFromLocalFileC3(
        local: File,
        payloadSize: Int,
        queue: OtaCommandQueue,
        startIndex: Int = 1,
        targetSlot : Int,
        onEnqueue: ((index: Int, payload: ByteArray) -> Unit)? = null,
        onProgress: (collected: Int, total: Int, percent: Int) -> Unit = { _, _, _ -> },
    ): OtaChunkPlan = withContext(Dispatchers.IO) {
        val total = local.length().toInt()
        val plan = planFor(total.toLong(), payloadSize)
        if (plan.endIndexNum == 0) {
            onProgress(0, 0, 0)
            return@withContext plan
        }

        val beginIdx = startIndex.coerceAtLeast(1)
        if (beginIdx > plan.endIndexNum) {
            onProgress(total, total, 100)
            return@withContext plan
        }

        // 시작 바이트 오프셋 = (beginIdx-1)*payloadSize
        var collectedBytes = ((beginIdx - 1) * payloadSize).coerceAtMost(total)

        BufferedInputStream(FileInputStream(local)).use { bis ->
            // skip()은 반환값을 확인하며 반복 호출
            var toSkip = collectedBytes
            while (toSkip > 0) {
                val skipped = bis.skip(toSkip.toLong()).toInt()
                if (skipped <= 0) break
                toSkip -= skipped
            }

            var i = beginIdx

            while (i <= plan.endIndexNum && isActive) {
                val need = if (i == plan.endIndexNum) plan.lastLen else payloadSize
                val buf = ByteArray(need)
                var off = 0
                while (off < need) {
                    val r = bis.read(buf, off, need - off)
                    if (r <= 0) break
                    off += r
                }

                if (off <= 0) break
                val payload = if (off == need) buf else buf.copyOf(off)

                /**enqueue 할때 dataclass안에다가 정보들 다 넣으면서 할까?말까**/
                val otaCommand = OtaCommand(
                    index = intTo3Bytes(i),
                    targetSlot = targetSlot,
                    payload = payload,
                    commandType = OtaCommandType.DATA_WRITE
                )
                queue.enqueue(otaCommand)
                onEnqueue?.invoke(i, payload)

                collectedBytes = (((i - 1) * payloadSize) + off).coerceAtMost(total)
                val pct = if (total <= 0) 0 else (collectedBytes * 100 / total).coerceIn(0, 100)
                onProgress(collectedBytes, total, pct)

                if ((i and 0x7F) == 0) yield() // 과도한 루프 방지
                i++
            }

        }

        // 최종 보정
        onProgress(total, total, 100)
        return@withContext plan
    }


    // 공용 계산 헬퍼
    data class OtaChunkPlan(
        val payloadSize: Int, // 16
        val endIndexNum: Int, // 마지막 인덱스 번호 (1..N)
        val lastLen: Int      // 마지막 payload 길이( remainder==0 -> 16 )
    )

    fun planFor(totalBytes: Long, payloadSize: Int = 16): OtaChunkPlan {
        val ceil = ((totalBytes + payloadSize - 1) / payloadSize).toInt()
        val rem = (totalBytes % payloadSize).toInt()
        val last = if (ceil == 0) 0 else if (rem == 0) payloadSize else rem
        return OtaChunkPlan(payloadSize, ceil, last)
    }
    fun intTo3Bytes(idx: Int): ByteArray {
        return byteArrayOf(
            ((idx shr 16) and 0xFF).toByte(), // MSB
            ((idx shr 8) and 0xFF).toByte(),  // Middle
            (idx and 0xFF).toByte()           // LSB
        )
    }

}

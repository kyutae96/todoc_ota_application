package com.todoc.todoc_ota_application.data.ota

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.todoc.todoc_ota_application.core.model.FileMeta
import com.todoc.todoc_ota_application.core.model.InfoResponse
import com.todoc.todoc_ota_application.core.model.OtaCommand
import com.todoc.todoc_ota_application.core.model.OtaCommandType
import com.todoc.todoc_ota_application.core.model.OtaFileProgress
import com.todoc.todoc_ota_application.core.model.OtaProgress
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.printLogBytesToString
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.toHex
import com.todoc.todoc_ota_application.data.ble.BleConnector
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_END_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_ERROR_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_START_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_WRITE_COMMAND
import com.todoc.todoc_ota_application.domain.ota.OtaLocalCollector.intTo3Bytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.LinkedList

class OtaCommandQueue(private val context: Context, private val connector: BleConnector,  private val onComplete: () -> Unit) {
    private val queue = LinkedList<OtaCommand>()
    private val originalTotalCommands = mutableListOf<OtaCommand>()
    private val TAG = this.javaClass.simpleName
    private var timeoutJob: Job? = null
    private var onTimeout: (() -> Unit)? = null
    private var isProcessing = false


    val scope = CoroutineScope(Dispatchers.Main + Job())
    val scopeIO = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun setOnTimeoutListener(listener: () -> Unit) {
        onTimeout = listener
    }


    private val filePlan: ArrayDeque<FileMeta> = ArrayDeque()
    private var activeFile: FileMeta? = null

    private var lastChunkTimeMs: Long = 0L
    private var avgMsPerChunk: Double = 0.0
    private val smoothAlpha = 0.2

    fun planFile(fileId: String, displayName: String?, totalChunks: Int) {
        require(totalChunks > 0) { "totalChunks must be > 0" }
        filePlan.add(FileMeta(fileId, displayName, totalChunks))
    }

    private fun emitPerFileProgress() {
        val f = activeFile ?: return
        val percent = ((f.processedChunks.toDouble() / f.totalChunks) * 100).toInt().coerceIn(0, 100)
        val remaining = (f.totalChunks - f.processedChunks).coerceAtLeast(0)
        val etaMs = (avgMsPerChunk * remaining).toLong().coerceAtLeast(0L)
        connector.onOtaFileProgress(
            OtaFileProgress(
                fileId = f.id,
                displayName = f.displayName,
                percent = percent,
                etaText = formatMillis(etaMs),
                processedChunks = f.processedChunks,
                totalChunks = f.totalChunks
            )
        )
    }


    fun clear(){
        originalTotalCommands.clear()
        queue.clear()
    }

    private fun startTimeout() {
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(2000L) // 2초 대기
//            onTimeout?.invoke()
        }
    }

    private fun cancelTimeout() {
        timeoutJob?.cancel()
    }


    /** 디버그 로그: 큐의 앞부분을 헥사로 덤프 */
    fun dumpForDebug(tag: String = "OTA-QUEUE") {
        Log.d(tag, "Queue size=${queue.size}")
        for ((commandId, targetSlot, payload, commandType) in queue) {
            Log.d(
                tag,
                "commandId=${commandId} \n" +
                        "targetSlot=${targetSlot} \n" +
                        "payload=${payload?.let { printLogBytesToString(it) }}\n" +
                        "commandType = ${commandType}"
            )


        }
    }

    fun enqueue(command: OtaCommand) {
        if (originalTotalCommands.contains(command)) return
        queue.add(command)
        originalTotalCommands.add(command)
        processNext()
    }

    private fun processNext() {
        if (isProcessing || queue.isEmpty()) return

        val command = queue.peek() ?: return

 /*       Log.d(
            TAG,
            "userId=${command.commandType} | " +
                    "index ${command.index?.let { it1 -> printLogBytesToString(it1) }} | " +
                    "targetSlot=${command.targetSlot} | " +
                    "payload=${command.payload?.let { it1 -> printLogBytesToString(it1) }} "
        )*/


        isProcessing = true
        startTimeout()
        scope.launch {
            when (command.commandType) {
                OtaCommandType.START_COMMAND -> {
                        val ok = connector.sendStartCommandByBle()
                        Log.d(TAG, "send HEADER_START_COMMAND result=$ok")
                }

                OtaCommandType.END_COMMAND -> {
                        val ok = connector.sendEndCommandByBle()
                        Log.d(TAG, "send HEADER_END_COMMAND result=$ok")
                }

                OtaCommandType.DATA_HEADER -> {
                        val ok = command.payload?.let { connector.sendWriteIndex0CommandByBle(payload = it) }
                        Log.d(TAG, "send HEADER_DATA_HEADER result=$ok")
                }

                OtaCommandType.DATA_WRITE -> {
                        val ok = command.payload?.let { connector.sendWriteDataCommandByBle(payload = it) }
                        Log.d(TAG, "send HEADER_DATA_WRITE result=$ok")
                }
            }
        }
    }

    fun onResponse(header: Byte, commandId: Byte) {
        Log.w("BLE_QUEUE", "onResponse() 수신: header=0x${header.toHex()}, commandId=$commandId")
        val command = queue.peek() ?: return

        val isStartOtaCommandComplete = header == HEADER_START_COMMAND &&
                command.commandType == OtaCommandType.START_COMMAND &&
                commandId == 0.toByte()
        val isStartOtaCommandError = header == HEADER_START_COMMAND &&
                command.commandType == OtaCommandType.START_COMMAND &&
                commandId != 0.toByte()

        val isEndOtaCommandComplete = header == HEADER_END_COMMAND &&
                command.commandType == OtaCommandType.END_COMMAND &&
                commandId == 0.toByte()
        val isEndOtaCommandError = header == HEADER_END_COMMAND &&
                command.commandType == OtaCommandType.END_COMMAND &&
                commandId != 0.toByte()

        val isDataHeaderCommandComplete = header == HEADER_WRITE_COMMAND &&
                command.commandType == OtaCommandType.DATA_HEADER &&
                commandId == 1.toByte()
        val isDataHeaderCommandError = header == HEADER_WRITE_COMMAND &&
                command.commandType == OtaCommandType.DATA_HEADER &&
                commandId != 1.toByte()

        val isDataWriteCommandComplete = header == HEADER_WRITE_COMMAND &&
                command.commandType == OtaCommandType.DATA_WRITE &&
                commandId == 1.toByte()
        val isDataWriteCommandError = header == HEADER_WRITE_COMMAND &&
                command.commandType == OtaCommandType.DATA_WRITE &&
                commandId != 1.toByte()

        val isErrorCommandResponse = header == HEADER_ERROR_COMMAND

        if (isErrorCommandResponse || isStartOtaCommandError || isEndOtaCommandError || isDataHeaderCommandError || isDataWriteCommandError) {
            Log.e(TAG, "맵 전송 실패: 명령 ${command.commandType}, payload= ${ command.payload?.let {printLogBytesToString(it)}}")
//            retryCurrentCommand()

            return
        }
        if (isStartOtaCommandComplete || isEndOtaCommandComplete || isDataHeaderCommandComplete || isDataWriteCommandComplete ) {
            Log.w(
                "OTA_QUEUE",
                "명령 처리 완료: commandType =${command.commandType}, payload= ${
                    command.payload?.let {
                        printLogBytesToString(
                            it
                        )
                    }
                }"
            )

            val now = System.currentTimeMillis()

            if (isDataHeaderCommandComplete) {
                // 새 파일 시작
                activeFile = if (filePlan.isNotEmpty()) filePlan.removeFirst() else null
                activeFile?.let {
                    it.processedChunks = 0
                    lastChunkTimeMs = now
                    avgMsPerChunk = 0.0
                    emitPerFileProgress() // 0% 최초 방출
                }
            }

            if (isDataWriteCommandComplete) {
                // 청크 1개 완료
                activeFile?.let { f ->
                    val dt = (now - (if (lastChunkTimeMs == 0L) now else lastChunkTimeMs)).coerceAtLeast(1L)
                    lastChunkTimeMs = now
                    avgMsPerChunk = if (avgMsPerChunk <= 0.0) dt.toDouble()
                    else (1 - smoothAlpha) * avgMsPerChunk + smoothAlpha * dt

                    f.processedChunks = (f.processedChunks + 1).coerceAtMost(f.totalChunks)
                    emitPerFileProgress()
                }
            }

            queue.poll()
            isProcessing = false
            cancelTimeout()

            if (queue.isEmpty()) {
                onComplete()
            } else {
                processNext()
            }
        }
//        else if (isInitCommand){
//            dismissProgressDialog()
//            dismissRetryDialog()
//            dismissErrorDialog()
//        }
    }

    private fun formatMillis(ms: Long): String {
        val totalSec = (ms + 500) / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return when {
            h > 0 -> String.format("%d:%02d:%02d", h, m, s)
            else -> String.format("%d:%02d", m, s)
        }
    }
//    fun onBleResponseProgressBar() {
//
//        val perCommandMs: Long = 100L    // 사이즈(=큐 원소)당 50ms 가정
//        val currentCommand = queue.peek() ?: return
//
//        val totalCommands = originalTotalCommands.size
//        if (totalCommands == 0) return
//
//        val processed = totalCommands - queue.size  // 이미 끝난 개수
//        val percent = ((processed.toDouble() / totalCommands) * 100).toInt().coerceIn(0, 100)+1
//
//        val remaining = (totalCommands - processed).coerceAtLeast(0)
//        val etaMs = remaining * perCommandMs
//
//        val slot = currentCommand.commandType
//        val msg = "${slot ?: "-"} Syncing ... ($percent%) • 남은시간 ${formatMillis(etaMs)}"
//        connector.onOtaFullResponse(msg)
//    }




    /**에러 상황(패킷 전송 실패)일 떄 queue 확인후 처음 명령 부터 다시 실행**/
    fun retryCurrentCommand() {
        Log.e(TAG, "전체 명령 다시 실행")
        queue.clear()
        Log.e(TAG, "현재 queue : ${queue}")
        Log.e(TAG, "현재 originalTotalCommands : ${originalTotalCommands}")
        isProcessing = false
        cancelTimeout()

        for (cmd in originalTotalCommands) {
            queue.add(cmd)
        }
        Log.e(TAG, "다시 명령 처리 시 queue : ${queue}")
        processNext()
    }
}

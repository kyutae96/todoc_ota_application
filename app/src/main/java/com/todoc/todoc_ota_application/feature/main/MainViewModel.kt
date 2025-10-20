package com.todoc.todoc_ota_application.feature.main


import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.le.ScanResult
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.integrity.p
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.todoc.todoc_ota_application.core.model.BleResponse
import com.todoc.todoc_ota_application.core.model.CollectProgress
import com.todoc.todoc_ota_application.core.model.InfoResponse
import com.todoc.todoc_ota_application.core.model.OtaCommand
import com.todoc.todoc_ota_application.core.model.OtaCommandType

import com.todoc.todoc_ota_application.core.model.OtaFileType
import com.todoc.todoc_ota_application.core.model.OtaPreparedFile
import com.todoc.todoc_ota_application.core.model.OtaPreparedPlan
import com.todoc.todoc_ota_application.core.model.OtaProgress
import com.todoc.todoc_ota_application.core.proto.PacketBuilder
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.packetMaker
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.printLogBytesToString
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.toHex
import com.todoc.todoc_ota_application.data.ble.BleConnector
import com.todoc.todoc_ota_application.data.ble.BleScanner
import com.todoc.todoc_ota_application.data.ble.ConnectionState
import com.todoc.todoc_ota_application.data.ble.PacketInfo
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_PASSWORD
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INDEX_INFO_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INDEX_SELECT_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INFO_COMMAND_BYTEARRAY
import com.todoc.todoc_ota_application.data.ble.ScanningState
import com.todoc.todoc_ota_application.data.ble.ServiceState
import com.todoc.todoc_ota_application.data.ota.ChunkQueue
import com.todoc.todoc_ota_application.data.ota.OtaCommandQueue
import com.todoc.todoc_ota_application.data.ota.WholeDownload
import com.todoc.todoc_ota_application.data.storage.OtaRefs.eventsCol
import com.todoc.todoc_ota_application.data.storage.OtaRefs.sessionRef
import com.todoc.todoc_ota_application.data.storage.OtaRefs.sessionsCol
import com.todoc.todoc_ota_application.domain.ota.OtaLocalCollector
import com.todoc.todoc_ota_application.domain.ota.OtaLocalCollector.intTo3Bytes
import com.todoc.todoc_ota_application.domain.ota.OtaLocalCollector.planFor
import com.todoc.todoc_ota_application.domain.ota.OtaOrder
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import kotlin.math.floor
import kotlin.math.min


class MainViewModel(app: Application) : AndroidViewModel(app) {
    // Firestore & 세션 상태
    private val db = FirebaseFirestore.getInstance()
    private var loggingDeviceName: String? = null
    private var loggingSessionId: String? = null


    private val scanner = BleScanner(app)
    val connector = BleConnector(app)
    private val TAG = this.javaClass.simpleName
    val devices: StateFlow<List<ScanResult>> = scanner.devices
    val scanning: StateFlow<ScanningState> = scanner.scanning
    val connection: StateFlow<ConnectionState> = connector.connection
    val packetFlow: StateFlow<List<ByteArray>> = connector.packetFlow
    val internalSerial: StateFlow<String> = connector.internalSerial
    val otaFileProgress = connector.otaFileProgress
    val bleResponseFlow = connector.bleResponseFlow
    val errorResponseFlow = connector.errorResponseFlow
    val detailedConnection = connector.detailedConnection
//    val otaFullProgress = connector.otaFullProgress

    val infoState = connector.infoState
    val selectState = connector.selectState
    private var previousConnectionState: ConnectionState? = null

    fun onOtaResponse(header: Byte, commandId: Byte) {
        txQueue.onResponse(header, commandId)
    }

    fun startScan() = scanner.start()
    fun stopScan() = scanner.stop()

    fun connectTo(result: ScanResult) {
        connector.connect(result.device)
    }

    fun disconnect(errorReason: String) {
        viewModelScope.launch {
            runCatching {
                finishOtaSession(
                    status = "error",
                    errorCode = errorReason
                )
            }.onFailure { e -> Log.e(TAG, "finishOtaSession disconnect", e) }
        }
        connector.disconnect()
    }

    fun justDisconnect() {
        connector.disconnect()
    }

    private var autoJob: Job? = null

    /**COLLECT**/
    // OTA 큐(한 파일씩 채워서 소비)
    val txQueue = OtaCommandQueue(context = app, connector = connector, onComplete = {
        _isOtaComplete.value = true
        viewModelScope.launch {
            runCatching {
                // currentSlotAfter: 선택 슬롯을 성공 가정 (정확히 알면 그 값으로)
                finishOtaSession(status = "success ota write")
            }.onFailure { e -> Log.e(TAG, "finishOtaSession tx", e) }
        }
    })

    private val _isOtaComplete = MutableStateFlow<Boolean?>(null)
    val isOtaComplete: StateFlow<Boolean?> = _isOtaComplete

    // 진행률(파일 단위)
    private val _progress = MutableStateFlow<CollectProgress?>(null)
    val progress: StateFlow<CollectProgress?> = _progress

    // ota진행률(파일 단위)
    private val _otaProgress = MutableStateFlow<OtaProgress?>(null)
    val otaProgress: StateFlow<OtaProgress?> = _otaProgress

    // 현재 처리 중인 파일 타입
    private val _current = MutableStateFlow<OtaFileType?>(null)
    val current: StateFlow<OtaFileType?> = _current

    // 준비 완료된 헤더 이벤트(파일 시작 전에 GATT로 Index=0 헤더 write 용)
    val headerEvent = MutableSharedFlow<OtaPreparedFile>(extraBufferCapacity = 1)

    // 전체 플랜(누적)
    private val _preparedPlan = MutableStateFlow<OtaPreparedPlan?>(null)
    val preparedPlan: StateFlow<OtaPreparedPlan?> = _preparedPlan

    private var job: Job? = null


    fun downloadDataToFile(base: String) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            txQueue.clear()
            try {
                for (t in OtaOrder.order) {
                    _current.value = t

                    val remote = OtaOrder.remotePath(base, t) // ex) "OTA/1/APP000.FEZ"
                    val local = File(getApplication<Application>().cacheDir, remote)
                    local.parentFile?.mkdirs()

                    val written = downloadFirebaseToFile(
                        remotePath = remote, local = local
                    ) { d, total ->
                        _progress.value = CollectProgress.of(
                            fileNum = t.num, downloaded = d, total = total
                        )
                    }

                    // 안전하게 100% 마무리
                    _progress.value = CollectProgress.of(
                        fileNum = t.num,
                        downloaded = written,
                        total = if (written > 0) written else 1 // 분모 0 방지
                    )
                    Log.d(TAG, "Downloaded ${t.name} -> ${local.absolutePath} (${written}B)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed: ${e.localizedMessage}", e)
//                viewModelScope.launch {
//                    kotlin.runCatching {
//                        finishOtaSession(status = "Download failed", errorCode = e.localizedMessage)
//                    }
//                }
            } finally {
                _current.value = null
            }
        }
    }

    private suspend fun downloadFirebaseToFile(
        remotePath: String, local: File, onProgress: (downloaded: Long, total: Long) -> Unit
    ): Long = withContext(Dispatchers.IO) {
        val ref = FirebaseStorage.getInstance().reference.child(remotePath)
        local.parentFile?.mkdirs()

        // 총 크기 먼저 확보 (없으면 -1L)
        val knownTotal = runCatching { ref.metadata.await().sizeBytes }.getOrDefault(-1L)

        // 시작 신호
        if (knownTotal > 0) onProgress(0L, knownTotal)

        val task = ref.getFile(local)
        task.addOnProgressListener { snap ->
            val total = if (snap.totalByteCount > 0) snap.totalByteCount else knownTotal
            val downloaded = snap.bytesTransferred
            if (total > 0) onProgress(downloaded, total)
        }

        // 완료 대기
        task.await()
        return@withContext local.length()
    }


    private fun ByteArray.hexHead(limit: Int = 16): String {
        val n = kotlin.math.min(size, limit)
        if (n <= 0) return ""
        val sb = StringBuilder(n * 3)
        for (i in 0 until n) {
            if (i > 0) sb.append(' ')
            sb.append(String.format("%02X", this[i].toInt() and 0xFF))
        }
        return sb.toString()
    }


    init {
        connector.passwordTimeoutMs = 10_000

        connector.passkeyProvider = suspend {
            val repo = LocalAuthRepository(app)
            repo.getPassKeyOrNull()
        }

        connector.buildPasswordPacket = { passKey ->
            packetMaker(HEADER_PASSWORD, passKey.toByteArray(), 5)
        }

        /*      viewModelScope.launch {
                  connector.notifications().collect { responsePacket ->
                      val packetHeader = responsePacket.firstOrNull()?.toInt()?.and(0xFF)?.toByte()
                      when(packetHeader){
                          HEADER_PASSWORD -> {

                          }
                      }
                  }
              }*/

//        viewModelScope.launch {
//            connector.otaFileProgress.collect { progress ->
//                progress ?: return@collect
//                val t = OtaFileType.fromName(progress.fileId)
//                _otaProgress.value =OtaProgress.ofCounts(
//                    fileNum = t?.num ?: 0,
//                    collected = progress.processedChunks,
//                    total = progress.totalChunks
//                )
//
//            }
//        }


        viewModelScope.launch {
            connection.collect { connectionState ->
                // 이전에 연결되어 있었는데 지금 끊어진 경우에만 처리
                val wasConnected = previousConnectionState is ConnectionState.Connected
                val isNowDisconnected = connectionState is ConnectionState.Disconnected

                if (wasConnected && isNowDisconnected) {
                    // OTA 세션이 진행 중이었다면 종료 처리
                    if (loggingSessionId != null) {
                        Log.w(TAG, "Connection lost during OTA session, finishing session...")
                        runCatching {
                            finishOtaSession(
                                status = "error",
                                errorCode = "Connection lost during OTA"
                            )
                        }.onFailure { e ->
                            Log.e(TAG, "Failed to finish OTA session on disconnect", e)
                        }
                    }
                }

                // 현재 상태를 이전 상태로 저장
                previousConnectionState = connectionState
            }
        }
    }


    val ready: StateFlow<Boolean> =
        connector.services.map { it is ServiceState.Ready && connector.isOtaReady() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    suspend fun sendStartCommand(controlType:Byte, target:Byte): Boolean {
        val payload = byteArrayOf(controlType,target)
        val packet = packetMaker(
            PacketInfo.HEADER_START_COMMAND, payload, 3
        )
        return connector.writeOta(packet)
    }

    suspend fun sendEndCommand(target:Byte): Boolean {
        val payload = byteArrayOf(target)
        val packet = packetMaker(
            PacketInfo.HEADER_END_COMMAND, payload, 1
        )
        return connector.writeOta(packet)
    }

    suspend fun sendInfoCommand(): Boolean {
        val packet = packetMaker(
            PacketInfo.HEADER_INFO_SELECT_COMMAND, INFO_COMMAND_BYTEARRAY, 2
        )
        return connector.writeOta(packet)
    }

    suspend fun sendSelectCommand(slotNum: Int): Boolean {
        val SELECT_COMMAND_BYTEARRAY = byteArrayOf(INDEX_SELECT_COMMAND, slotNum.toByte())
        val packet = packetMaker(
            PacketInfo.HEADER_INFO_SELECT_COMMAND, SELECT_COMMAND_BYTEARRAY, 3
        )
        return connector.writeOta(packet)
    }

    suspend fun sendResetCommand(): Boolean {
        val RESET_COMMAND_BYTEARRAY = byteArrayOf(INDEX_SELECT_COMMAND, 0xFF.toByte())
        val packet = packetMaker(
            PacketInfo.HEADER_INFO_SELECT_COMMAND, RESET_COMMAND_BYTEARRAY, 3
        )
        return connector.writeOta(packet)
    }


    /** 주어진 이름과 정확히 일치하는 광고명을 자동 연결 시도 (timeout 6s 내) */
    @SuppressLint("MissingPermission")
    fun tryAutoConnectByName(exactName: String, timeoutMs: Long = 6000, onTimeout: () -> Unit) {
        autoJob?.cancel()
        autoJob = viewModelScope.launch {
            startScan()
            val matched: ScanResult? = withTimeoutOrNull(timeoutMs) {

                val list = devices.first { l ->
                    l.any { (it.device.name ?: "") == exactName }
                }
                // 정확히 일치하는 첫 항목 반환
                list.first { (it.device.name ?: "") == exactName }
            }
            stopScan()

            if (matched != null) {
                connectTo(matched)
            } else {
                onTimeout()
            }
        }
    }


    /**
     * Start → [각 파일: C3 Index=0 → C3 데이터 인덱스들] → End
     * 순서로 OtaTxOperations 리스트를 만들고, 마지막에 txQueue에 한 번에 enqueue.
     */
    @SuppressLint("MissingPermission")
    fun stageOtaOpsThenEnqueueC3(
        userId: String?,
        base: String,
        slotNum: Int,
        order: List<OtaFileType>,
        payloadSize: Int = 16  // 스펙상 16B 권장 (MTU 네고 안 함)
    ) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            // [LOGGING ADD] --- 세션 시작 준비값 뽑기 ---
            val deviceName = when (val c = connection.value) {
                is ConnectionState.Connected -> c.device.name ?: c.device.address ?: "unknown"
                else -> "unknown"
            }
            val deviceAddress = when (val c = connection.value) {
                is ConnectionState.Connected -> c.device.address ?: c.device.address ?: "unknown"
                else -> "unknown"
            }
            val deviceId = internalSerial.value.ifBlank { deviceName }                 // 내부기 시리얼 우선
            val sourcePath =
                base                                                     // ex) "OTA/1.2.3"
            val files =
                order.map { it.name }                                         // ["MANIFEST","APP000",...]
            val preSlot: Int = slotNum
            val appVersion = try {
                getApplication<Application>().packageManager.getPackageInfo(
                    getApplication<Application>().packageName,
                    0
                ).versionName ?: "0.0.0"
            } catch (_: Exception) {
                "0.0.0"
            }

            runCatching {
                startOtaSessionIfNeeded(
                    deviceId = deviceId,
                    userId = userId,
                    slotSelected = null,
                    sourcePath = sourcePath,
                    files = files,
                    chunkSize = payloadSize,
                    preSlot = preSlot,              //어디에 썼는지
                    appVersion = appVersion,
                    deviceName = deviceName,
                    deviceAddress = deviceAddress
                )
            }.onFailure { e -> Log.e(TAG, "startOtaSessionIfNeeded failed", e) }


            val prepared = mutableListOf<OtaPreparedFile>()
            val operations = mutableListOf<OtaCommand>()

            // 0) START(C0)
            operations += OtaCommand(
                index = null,
                targetSlot = slotNum,
                payload = null,
                commandType = OtaCommandType.START_COMMAND
            )

            for (t in order) {
                _current.value = t

                // 1) 로컬 파일 경로
                val remote = OtaOrder.remotePath(base, t) // ex) "OTA/1/APP000.FEZ"
                val local = File(getApplication<Application>().cacheDir, remote)
                local.parentFile?.mkdirs()

                val total = local.length()
                if (total <= 0L) {
                    Log.w(TAG, "Skip empty file: ${local.absolutePath}")
                    continue
                }

                // 2) EndIndexNum / EndIndexByte 계산 (ceil, remainder==0 => lastLen=payloadSize)
                val endIndexNum = ((total + payloadSize - 1) / payloadSize).toInt()
                val rem = (total % payloadSize).toInt()
                val lastLen = if (rem == 0) payloadSize else rem

                txQueue.planFile(
                    fileId = t.name,                  // ex) "APP000"
                    displayName = local.name,         // ex) "app000.fez"
                    totalChunks = endIndexNum         // C3 Index=1..endIndexNum
                )

                // 3) C3 Index=0 헤더 → operations
                val c3Header = PacketBuilder.c3WriteHeader(
                    slotNum = slotNum,
                    fileNum = t.num,
                    fileLength = total,
                    endIndexNum = endIndexNum.toLong(),
                    endIndexByte = lastLen
                )
                // 3) C3 c3withOutHeader
                val c3withOutHeader = PacketBuilder.c3WriteWithOutHeader(
                    slotNum = slotNum,
                    fileNum = t.num,
                    fileLength = total,
                    endIndexNum = endIndexNum.toLong(),
                    endIndexByte = lastLen
                )
                val pf = OtaPreparedFile(
                    type = t,
                    remotePath = remote,
                    localFile = local,
                    length = total,
                    endIndexNum = endIndexNum,
                    endIndexByte = lastLen,
                    headerIndex0 = c3Header
                )
                prepared += pf
                _preparedPlan.value = OtaPreparedPlan(
                    base = base,
                    slotNum = slotNum,
                    chunkSize = payloadSize,
                    files = prepared.toList()
                )

                operations += OtaCommand(
                    index = intTo3Bytes(0),
                    targetSlot = slotNum,
                    payload = c3withOutHeader,
                    commandType = OtaCommandType.DATA_HEADER
                )

                // 4) 파일 데이터를 16B씩 잘라 C3 데이터 누적
                withContext(Dispatchers.IO) {
                    var idx = 1
                    var collected = 0
                    BufferedInputStream(FileInputStream(local)).use { bis ->
                        while (idx <= endIndexNum) {
                            val need = if (idx == endIndexNum) lastLen else payloadSize
                            val buf = ByteArray(need)
                            var off = 0
                            while (off < need) {
                                val r = bis.read(buf, off, need - off)
                                if (r <= 0) break
                                off += r
                            }
                            if (off <= 0) break

                            val payload = if (off == need) buf else buf.copyOf(off)
                            val body =
                                PacketBuilder.c3WriteDataPayload(index = idx, payload = payload)
                            operations += OtaCommand(
                                index = intTo3Bytes(idx),
                                targetSlot = slotNum,
                                payload = body,
                                commandType = OtaCommandType.DATA_WRITE
                            )

                            collected = min(collected + off, total.toInt())

                            idx++
                        }
                    }
                }

                Log.d(
                    TAG,
                    "Prepared ${t.name}: endIndexNum=$endIndexNum lastLen=$lastLen operations Size=${operations.size}"
                )
            }

            // 5) END(C1)
            operations += OtaCommand(
                index = null,
                targetSlot = slotNum,
                payload = null,
                commandType = OtaCommandType.END_COMMAND
            )

            // ===== 최종: 한 번에 enqueue =====
            txQueue.clear()
            operations.forEach {
                txQueue.enqueue(it)
            }

//            txQueue.dumpForDebug(tag = "OTA-TXQ")d

            _current.value = null
        }
    }
//    }


    // [LOGGING ADD] 세션 시작
    private suspend fun startOtaSessionIfNeeded(
        deviceId: String,
        userId: String?,
        slotSelected: Int?,
        sourcePath: String,
        files: List<String>,
        chunkSize: Int,
        preSlot: Int?,
        appVersion: String,
        deviceName: String,
        deviceAddress: String,
    ) {
        if (loggingSessionId != null) return
        loggingDeviceName = deviceName
        val doc = sessionsCol(db, deviceName).document()
        val data = hashMapOf(
            "deviceId" to deviceId,
            "userId" to userId,       // <--------추후 어떤 사람이 했는지 알 수 있게
            "status" to "running",
            "slotSelected" to null,
            "sourcePath" to sourcePath,
            "files" to files,
            "chunkSize" to chunkSize,
            "preSlot" to preSlot,
            "appVersion" to appVersion,
            "deviceName" to deviceName,
            "deviceAddress" to deviceAddress,
            "startedAt" to FieldValue.serverTimestamp(),
            "endedAt" to null
        )
        val serverTime = FieldValue.serverTimestamp()

        db.runBatch { b ->
            b.set(doc, data)
            b.set(
                eventsCol(db, deviceName, doc.id).document(), mapOf(
                    "type" to "sessionStart",
                    "at" to serverTime,
                    "slot" to preSlot,
                    "message" to "OTA start"
                )
            )
        }.await()
        loggingSessionId = doc.id
    }

    // 진행률/일반 이벤트
    suspend fun logEvent(
        type: String,
        slot: Int? = null,
        fileId: String? = null,
        percent: Int? = null,
        processedChunks: Int? = null,
        totalChunks: Int? = null,
        message: String? = null
    ) {
        val dev = loggingDeviceName ?: return
        val sid = loggingSessionId ?: return
        val serverTime = FieldValue.serverTimestamp()
        val m = mutableMapOf<String, Any?>(
            "type" to type,
            "at" to serverTime,
            "slot" to slot,
            "fileId" to fileId,
            "percent" to percent,
            "processedChunks" to processedChunks,
            "totalChunks" to totalChunks,
            "message" to message
        ).filterValues { it != null }

        eventsCol(db, dev, sid).add(m).await()
    }

    // 세션 종료
    suspend fun finishOtaSession(
        status: String, errorCode: String? = null, currentSlotAfter: Int? = null
    ) {
        Log.w(
            TAG,
            "loggingDeviceName : ${loggingDeviceName}  |  loggingSessionId : ${loggingSessionId}"
        )
        val dev = loggingDeviceName ?: return
        val sid = loggingSessionId ?: return
        val updates = mutableMapOf<String, Any>(
            "status" to status, "endedAt" to FieldValue.serverTimestamp()
        )
        if (errorCode != null) updates["errorCode"] = errorCode
        if (currentSlotAfter != null) updates["currentSlotAfter"] = currentSlotAfter

        db.runBatch { b ->
            b.update(sessionRef(db, dev, sid), updates)
            b.set(
                eventsCol(db, dev, sid).document(), mapOf(
                    "type" to "sessionEnd",
                    "at" to FieldValue.serverTimestamp(),
                    "message" to status
                )
            )
        }.await()

        loggingDeviceName = null
        loggingSessionId = null
    }

    suspend fun selectOtaSession(
        status: String,
        errorCode: String? = null,
        slotSelected: Int? = null,
        sessionType: String? = null
    ) {
        Log.w(
            TAG,
            "loggingDeviceName : ${loggingDeviceName}  |  loggingSessionId : ${loggingSessionId}"
        )
        val dev = loggingDeviceName ?: return
        val sid = loggingSessionId ?: return
        val updates = mutableMapOf<String, Any>(
            "status" to status
//            , "endedAt" to FieldValue.serverTimestamp()
        )
        if (errorCode != null) updates["errorCode"] = errorCode
        if (slotSelected != null) updates["slotSelected"] = slotSelected

        db.runBatch { b ->
            b.update(sessionRef(db, dev, sid), updates)
            b.set(
                eventsCol(db, dev, sid).document(), mapOf(
                    "type" to sessionType,
//                    "type" to "sessionEnd",
                    "at" to FieldValue.serverTimestamp(),
                    "message" to status
                )
            )
        }.await()

        loggingDeviceName = null
        loggingSessionId = null
    }


    suspend fun getLatestSourcePath(deviceName: String): String? {
        return try {
            val snapshot = sessionsCol(db, deviceName).orderBy(
                "startedAt",
                com.google.firebase.firestore.Query.Direction.DESCENDING
            ).limit(1).get().await()

            if (!snapshot.isEmpty) {
                val fullPath = snapshot.documents.first().getString("sourcePath")
                fullPath?.substringAfter("OTA/") // "OTA/" 제거 → "ver0.0"
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLatestSourcePath failed: ${e.localizedMessage}", e)
            null
        }
    }


    suspend fun getLatestOtaFolder(): String? {
        return try {
            val storage = FirebaseStorage.getInstance()
            val otaRef = storage.reference.child("OTA")

            // 최상위 OTA 하위 폴더들 가져오기
            val prefixes = otaRef.listAll().await().prefixes
            if (prefixes.isEmpty()) return null

            var latestFolder: String? = null
            var latestTime: Long = -1

            for (folderRef in prefixes) {
                // 해당 폴더 안에 있는 파일 중 가장 최신 시간 확인
                val items = folderRef.listAll().await().items
                val newestFileTime =
                    items.mapNotNull { it.metadata.await().updatedTimeMillis }.maxOrNull() ?: 0L

                if (newestFileTime > latestTime) {
                    latestTime = newestFileTime
                    latestFolder = folderRef.name
                }
            }

            latestFolder
        } catch (e: Exception) {
            Log.e(TAG, "getLatestOtaFolder failed: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun isLatestSourcePathMatching(deviceName: String): Boolean {
        return try {
            val latestPath = getLatestSourcePath(deviceName)      // ex) "ver0.0"
            val latestFolder = getLatestOtaFolder()               // ex) "ver0.0"

            if (latestPath == null || latestFolder == null) {
                false
            } else {
                latestPath == latestFolder
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "isLatestSourcePathMatching failed: ${e.localizedMessage}", e)
            false
        }
    }

    /** 개별 txt 경로에서 텍스트 읽기 */
    suspend fun loadTxtFromStorage(path: String): String? {
        return try {
            val ref = FirebaseStorage.getInstance().reference.child(path)
            val bytes = ref.getBytes(1 * 1024 * 1024).await() // 최대 1MB
            bytes.toString(Charset.defaultCharset())
        } catch (e: Exception) {
            Log.e(TAG, "loadTxtFromStorage failed: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun setTxtFromLatestOtaFolder(
        fileName: String, textView: TextView, progressBar: ProgressBar
    ) {
        try {
            // Progress 표시
            progressBar.visibility = View.VISIBLE
            textView.text = ""

            val storage = FirebaseStorage.getInstance()
            val otaRef = storage.reference.child("OTA")

            val prefixes = otaRef.listAll().await().prefixes
            if (prefixes.isEmpty()) {
                textView.text = "OTA 폴더가 비어 있습니다."
                return
            }

            // 최신 폴더 찾기
            var latestFolder: String? = null
            var latestTime = -1L
            for (folderRef in prefixes) {
                val items = folderRef.listAll().await().items
                val newestFileTime = items.mapNotNull {
                    runCatching { it.metadata.await().updatedTimeMillis }.getOrNull()
                }.maxOrNull() ?: 0L
                if (newestFileTime > latestTime) {
                    latestTime = newestFileTime
                    latestFolder = folderRef.name
                }
            }

            if (latestFolder == null) {
                textView.text = "최신 OTA 폴더를 찾지 못했습니다."
                return
            }

            // txt 파일 읽기
            val path = "OTA/$latestFolder/$fileName"
            val ref = storage.reference.child(path)
            val bytes = ref.getBytes(1 * 1024 * 1024).await()
            val content = bytes.toString(Charset.defaultCharset())

            textView.text = content
        } catch (e: Exception) {
            Log.e(TAG, "setTxtFromLatestOtaFolder failed: ${e.localizedMessage}", e)
            textView.text = "readme 파일이 존재하지 않습니다."
        } finally {
            // Progress 숨기기
            progressBar.visibility = View.GONE
        }
    }


    data class SlotUiParams(
        val currentBootSlot: Int?,      // 1/2/ null
        val slot1Date: String,          // "yyyy.MM.dd" 또는 "—"
        val slot2Date: String,          // "
        val isSlot1RecentUpdated: Boolean, val isSlot2RecentUpdated: Boolean
    )

    /** infoState → 현재 부팅 슬롯 (01/02 → 1/2) */
    private fun currentBootSlotFromInfo(): Int? {
        val hex = infoState.value?.currentBootSlotNum?.toHex()?.uppercase() ?: return null
        Log.w(TAG, "currentBootSlotFromInfo() : ${hex}")
        return when (hex) {
            "01" -> 1; "02" -> 2; "FF" -> 0; else -> null
        }
    }

    private fun formatYmd(ts: com.google.firebase.Timestamp?): String {
        if (ts == null) return "------"
        val sdf = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.KOREA)
        sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(ts.toDate())
    }

    /** 특정 슬롯의 가장 최근 세션 startedAt */
    private suspend fun getLastStartedAt(
        deviceName: String, slot: Int
    ): com.google.firebase.Timestamp? {
        return try {
            val snap = sessionsCol(db, deviceName).whereEqualTo("preSlot", slot)
                .whereEqualTo("status", "success ota write")
                .orderBy("startedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1).get().await()
            Log.w(TAG, "getLastStartedAt(slot=$slot) : ${snap.documents}")
            if (!snap.isEmpty) snap.documents.first().getTimestamp("startedAt") else null
        } catch (e: Exception) {
            Log.e(TAG, "getLastStartedAt(slot=$slot) failed: ${e.localizedMessage}", e)
            null
        }
    }

    /** slotSelected의 슬롯의 가장 최근 세션 startedAt */
    private suspend fun getBootLastStartedAt(
        deviceName: String, slot: Int
    ): com.google.firebase.Timestamp? {
        return try {
            val snap = sessionsCol(db, deviceName).whereEqualTo("slotSelected", slot)
                .whereEqualTo("status", "success")
                .orderBy("startedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1).get().await()
            Log.w(TAG, "getLastStartedAt(slot=$slot) : ${snap.documents}")
            if (!snap.isEmpty) snap.documents.first().getTimestamp("startedAt") else null
        } catch (e: Exception) {
            Log.e(TAG, "getLastStartedAt(slot=$slot) failed: ${e.localizedMessage}", e)
            null
        }
    }

    /** 현재 연결된 기기 기준으로 BottomSheet 파라미터 빌드 */
    @SuppressLint("MissingPermission")
    suspend fun buildSlotUiParamsForCurrentDevice(): SlotUiParams {
        val deviceName = when (val c = connection.value) {
            is ConnectionState.Connected -> c.device.name ?: c.device.address ?: "unknown"
            else -> "unknown"
        }

        Log.w(TAG, "deviceName : ${deviceName}")

        // 최근 각 슬롯 업데이트 날짜
        val s1 = getLastStartedAt(deviceName, 1)
        val s2 = getLastStartedAt(deviceName, 2)

        Log.w(TAG, "buildSlotUiParamsForCurrentDevice() : ${s1} ${s2}")
        val d1 = formatYmd(s1)
        val d2 = formatYmd(s2)
        Log.w(TAG, "buildSlotUiParamsForCurrentDevice() : ${d1} ${d2}")


        // "최근 업데이트 됨" 뱃지: 더 최근인 쪽에만 true (동일/둘다 null이면 전부 false)
        val is1Newer = (s1 != null && (s2 == null || s1.seconds > s2.seconds))
        val is2Newer = (s2 != null && (s1 == null || s2.seconds > s1.seconds))
        val recent1 = is1Newer && !(s2 != null && s1?.seconds == s2.seconds)
        val recent2 = is2Newer && !(s1 != null && s1?.seconds == s2?.seconds)
        Log.w(TAG, "is1Newer : ${is1Newer}")
        Log.w(TAG, "is2Newer : ${is2Newer}")
        Log.w(TAG, "recent1 : ${recent1}")
        Log.w(TAG, "recent2 : ${recent2}")

        return SlotUiParams(
            currentBootSlot = currentBootSlotFromInfo(),
            slot1Date = d1,
            slot2Date = d2,
            isSlot1RecentUpdated = recent1,
            isSlot2RecentUpdated = recent2
        )
    }


    /** Select 부팅할 슬롯 현재 연결된 기기 기준으로 BottomSheet 파라미터 빌드 */
    @SuppressLint("MissingPermission")
    suspend fun buildBootSlotUiParamsForCurrentDevice(): SlotUiParams {
        val deviceName = when (val c = connection.value) {
            is ConnectionState.Connected -> c.device.name ?: c.device.address ?: "unknown"
            else -> "unknown"
        }

        Log.w(TAG, "deviceName : ${deviceName}")

        // 최근 각 슬롯 업데이트 날짜
        val s1 = getBootLastStartedAt(deviceName, 1)
        val s2 = getBootLastStartedAt(deviceName, 2)

        Log.w(TAG, "buildSlotUiParamsForCurrentDevice() : ${s1} ${s2}")
        val d1 = formatYmd(s1)
        val d2 = formatYmd(s2)
        Log.w(TAG, "buildSlotUiParamsForCurrentDevice() : ${d1} ${d2}")


        // "최근 업데이트 됨" 뱃지: 더 최근인 쪽에만 true (동일/둘다 null이면 전부 false)
        val is1Newer = (s1 != null && (s2 == null || s1.seconds > s2.seconds))
        val is2Newer = (s2 != null && (s1 == null || s2.seconds > s1.seconds))
        val recent1 = is1Newer && !(s2 != null && s1?.seconds == s2.seconds)
        val recent2 = is2Newer && !(s1 != null && s1?.seconds == s2?.seconds)
        Log.w(TAG, "is1Newer : ${is1Newer}")
        Log.w(TAG, "is2Newer : ${is2Newer}")
        Log.w(TAG, "recent1 : ${recent1}")
        Log.w(TAG, "recent2 : ${recent2}")

        return SlotUiParams(
            currentBootSlot = currentBootSlotFromInfo(),
            slot1Date = d1,
            slot2Date = d2,
            isSlot1RecentUpdated = recent1,
            isSlot2RecentUpdated = recent2
        )
    }
}
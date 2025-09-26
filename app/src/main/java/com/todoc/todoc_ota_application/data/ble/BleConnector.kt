package com.todoc.todoc_ota_application.data.ble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.todoc.todoc_ota_application.App
import com.todoc.todoc_ota_application.core.model.BleResponse
import com.todoc.todoc_ota_application.core.model.ErrorResponse
import com.todoc.todoc_ota_application.core.model.InfoResponse
import com.todoc.todoc_ota_application.core.model.OtaCommandType
import com.todoc.todoc_ota_application.core.model.OtaFileProgress
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.byteExtractor
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.c2Select
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.c3WriteWithOutHeader
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.packetMaker
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.printLogBytesToString
import com.todoc.todoc_ota_application.data.ble.PacketInfo.END_RESULT_ACCEPT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.ERROR_RESULT_EZAIRO_COMM_ERROR
import com.todoc.todoc_ota_application.data.ble.PacketInfo.ERROR_RESULT_INVALID_OTA_ORDER
import com.todoc.todoc_ota_application.data.ble.PacketInfo.ERROR_RESULT_LOW_BATTERY
import com.todoc.todoc_ota_application.data.ble.PacketInfo.ERROR_RESULT_NONE
import com.todoc.todoc_ota_application.data.ble.PacketInfo.ERROR_RESULT_NOT_OTA_MODE
import com.todoc.todoc_ota_application.data.ble.PacketInfo.ERROR_RESULT_UNKNOWN_ERROR
import com.todoc.todoc_ota_application.data.ble.PacketInfo.GAIA_HEADER_PAIRING_REQUEST
import com.todoc.todoc_ota_application.data.ble.PacketInfo.GAIA_HEADER_RESPONSE
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_AUDIO_INPUT_MAX_READ
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_END_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_ERROR_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_INFO_SELECT_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_PASSWORD
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_SOUND_PROCESSOR_INFO
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_SOUND_PROCESSOR_STATUS
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_START_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_WRITE_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INDEX_CHOOSE_FILE_HEADER_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INDEX_INFO_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INDEX_SELECT_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INFO_RESULT_ACCEPT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INFO_RESULT_REJECT_CURRENT_BOOT_SLOT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INFO_RESULT_REJECT_INVALID_SLOT_NUM
import com.todoc.todoc_ota_application.data.ble.PacketInfo.SELECT_RESULT_ACCEPT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.SELECT_RESULT_REJECT_CURRENT_BOOT_SLOT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.SELECT_RESULT_REJECT_INVALID_SLOT_NUM
import com.todoc.todoc_ota_application.data.ble.PacketInfo.START_RESULT_ACCEPT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.START_RESULT_ERR_EZAIRO_COMM
import com.todoc.todoc_ota_application.data.ble.PacketInfo.START_RESULT_ERR_LOW_BATTERY
import com.todoc.todoc_ota_application.data.ble.PacketInfo.START_RESULT_ERR_UNKNOWN
import com.todoc.todoc_ota_application.data.ble.PacketInfo.WRITE_0_RESULT_ACCEPT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.WRITE_0_RESULT_ERR_CURRENT_BOOT_SLOT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.WRITE_0_RESULT_INVALID_DATA_INDEX_ORDER
import com.todoc.todoc_ota_application.data.ble.PacketInfo.WRITE_0_RESULT_NO_FILE_EXIST
import com.todoc.todoc_ota_application.data.ble.PacketInfo.WRITE_RESULT_ACCEPT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.WRITE_RESULT_ERR_CURRENT_BOOT_SLOT
import com.todoc.todoc_ota_application.data.ble.PacketInfo.WRITE_RESULT_INVALID_DATA_INDEX_ORDER
import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.and

class BleConnector(private val context: Context) {
    private val TAG = this.javaClass.simpleName
    private var gatt: BluetoothGatt? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    //    private val _otaFullProgress = MutableStateFlow<String?>(null)
//    val otaFullProgress: StateFlow<String?> = _otaFullProgress
//    fun onOtaFullResponse(result: String) {
//        _otaFullProgress.value = result
//    }
    private var commandTimeoutJob: Job? = null
    private val commandTimeoutMs: Long = 10_000


    private val mPacketSendScope = CoroutineScope(Dispatchers.Main + Job())

    private val _packetFlow = MutableStateFlow<List<ByteArray>>(emptyList())
    val packetFlow: StateFlow<List<ByteArray>> = _packetFlow
    fun writeCurrentPacket(message: ByteArray) {
        val currentPacket = _packetFlow.value.toMutableList()
        currentPacket.add(message)
        _packetFlow.value = currentPacket
    }

    private val _otaFileProgress = MutableStateFlow<OtaFileProgress?>(null)
    val otaFileProgress: StateFlow<OtaFileProgress?> = _otaFileProgress

    fun onOtaFileProgress(p: OtaFileProgress) {
        _otaFileProgress.value = p
    }

    private val emitterScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _bleResponseFlow = MutableSharedFlow<BleResponse>(
        replay = 0,
        extraBufferCapacity = 256,                 // 빠른 연속 응답 대비
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val bleResponseFlow: SharedFlow<BleResponse> = _bleResponseFlow

    fun onBleResponse(header: Byte, commandId: Byte, responseData: ByteArray?) {
        _bleResponseFlow.tryEmit(
            BleResponse(header, commandId, responseData?.copyOf())
        )
    }

    private val _errorResponseFlow = MutableSharedFlow<ErrorResponse>(
        replay = 0,
        extraBufferCapacity = 256,                 // 빠른 연속 응답 대비
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorResponseFlow: SharedFlow<ErrorResponse> = _errorResponseFlow

    fun onErrorResponse(header: Byte, rspCode: Byte, message: String) {
        _errorResponseFlow.tryEmit(
            ErrorResponse(header, rspCode, message)
        )
    }


    /**C2 select vm**/
    private val _selectState = MutableStateFlow<Int>(0)
    val selectState = _selectState.asStateFlow()
    fun onSelectResponse(result: Int) {
        _selectState.value = result
    }

    /**C2 info vm**/
    private val _infoState = MutableStateFlow<InfoResponse?>(null)
    val infoState = _infoState.asStateFlow()
    fun onInfoResponse(
        result: Int,
        versionMajor: Int,
        versionMinor: Int,
        currentBootSlotNum: Byte,
        preBootSlotNum: Byte
    ) {
        _infoState.value =
            InfoResponse(result, versionMajor, versionMinor, currentBootSlotNum, preBootSlotNum)
    }


    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connection: StateFlow<ConnectionState> = _connection

    val _internalSerial = MutableStateFlow<String>("------")
    val internalSerial: StateFlow<String> = _internalSerial

    private val _bond = MutableStateFlow<BondState>(BondState.None)
    val bond: StateFlow<BondState> = _bond

    private val _services = MutableStateFlow<ServiceState>(ServiceState.Idle)
    val services: StateFlow<ServiceState> = _services

    // ---- OTA handles
    private var otaService: BluetoothGattService? = null
    private var otaTx: BluetoothGattCharacteristic? = null
    private var otaRx: BluetoothGattCharacteristic? = null
    private var otaRxCccd: BluetoothGattDescriptor? = null

    // RX notifications
    private val _rx: MutableSharedFlow<ByteArray> =
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    val rx: SharedFlow<ByteArray> = _rx

    /** 패스키를 비동기로 제공 (DB/DataStore 등) */
    var passkeyProvider: (suspend () -> String?)? = null

    /** 제공받은 패스키를 OTA 전송용 바이트로 구성 */
    var buildPasswordPacket: ((String) -> ByteArray)? = null


    /** 인증 대기 타임아웃(ms) 기본 10초 */
    var passwordTimeoutMs: Long = 10_000
    var soundProcessorTimeoutMs: Long = 10_000
    var packetTimeoutMs: Long = 10_000

    // 내부 코루틴 스코프 (Connector 수명과 함께)
    private var passwordTimeoutJob: Job? = null
    private var soundProcessorInfoTimeoutJob: Job? = null
    private var packetTimeoutJob: Job? = null

    private var receiversRegistered = false

    init {
        registerReceivers()
    }

    fun close() {
        unregisterReceivers()
        disconnect()
        scope.cancel()
    }


    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        if (device.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            Log.e(TAG, "Classic-only device → skip LE connect")
            return
        }
        Log.w(TAG, "will be connect to : ${device.name}")
        _connection.value = ConnectionState.Connecting
        _services.value = ServiceState.Idle
        clearOtaHandles()
        gatt?.close(); gatt = null

        val mGaiaGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(g, status, newState)
                if (status != BluetoothGatt.GATT_SUCCESS && newState != BluetoothProfile.STATE_CONNECTED) {
                    _connection.value = ConnectionState.Disconnected
                    g.close()
                    return
                }
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "STATE_CONNECTED")
                        if (Build.VERSION.SDK_INT >= 21) {
                            g.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        }
                        if (Build.VERSION.SDK_INT >= 26) {
                            g.setPreferredPhy(
                                BluetoothDevice.PHY_LE_2M_MASK,
                                BluetoothDevice.PHY_LE_2M_MASK,
                                BluetoothDevice.PHY_OPTION_NO_PREFERRED
                            )
                        }

//                        g.discoverServices()


                        if (g.device.bondState == BluetoothDevice.BOND_BONDED) {
                            Log.d(TAG, "본딩 성공.")

                            if (!g.discoverServices()) {
                                Log.d(TAG, "onConnectionStateChange : Failed to discover services.")
                                g.disconnect()
                            }
                        } else {
                            Log.d(TAG, "본딩을 시도합니다.")
                            g.device.createBond()   // createbond를 하면 클래식도 같이 연결됨
                        }

                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(TAG, "STATE_DISCONNECTED")
                        _connection.value = ConnectionState.Disconnected
                        _bond.value = BondState.None
                        _services.value = ServiceState.Idle
                        g.close()
                        clearOtaHandles()
                    }
                }
            }


            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(g, status)
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    _services.value = ServiceState.Failed(status); return
                }
                Log.d(TAG, "=== Service Discovery Debug Info ===")
                Log.d(TAG, "Device: ${g.device.name} (${g.device.address})")
                Log.d(TAG, "Android Version: ${Build.VERSION.SDK_INT}")
                Log.d(TAG, "Device Model: ${Build.MODEL}")
                Log.d(TAG, "Total services found: ${g.services.size}")

                // ---- Find OTA service & chars
                val svc = g.getService(OtaGattSpec.QUALCOMM_UUID)
                if (svc == null) {
                    Log.d(TAG, "service null")
                    _connection.value = ConnectionState.Disconnected
                    _services.value = ServiceState.Failed(-1) // 서비스 없음
                    return
                }
                Log.d(TAG, "service pass")
                val tx = svc.getCharacteristic(OtaGattSpec.GaiaCommand_UUID)
                val rx = svc.getCharacteristic(OtaGattSpec.GaiaResponse_UUID)

                otaService = svc
                otaTx = tx
                otaRx = rx

                ///

                val props = rx!!.properties
                val supportsNotify = props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
                val supportsIndicate = props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0

                if (!supportsNotify && !supportsIndicate) {
                    Log.e(TAG, "이 characteristic은 알림/인디케이션을 지원하지 않습니다.")
                    return
                }

                val cccd = rx.getDescriptor(OtaGattSpec.CCCD)

                otaRxCccd = cccd

                if (otaRxCccd == null) {
                    Log.d(TAG, "onServiceDiscovered : Failed to get descriptor.")
                    _connection.value = ConnectionState.Disconnected
                    _services.value = ServiceState.Failed(-2)
                    return
                }

                if (!g.setCharacteristicNotification(
                        rx,
                        true
                    )
                ) {
                    Log.d(TAG, "onServiceDiscovered : Failed to enable notification setting.")
                    _connection.value = ConnectionState.Disconnected
                    _services.value = ServiceState.Failed(-2)
                    return
                }
                CoroutineScope(Dispatchers.Main).launch {
                    cccd?.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE

                    delay(300)
                    val secondResult = g?.writeDescriptor(cccd)
                    Log.d(TAG, "ServerToClient descriptor 설정 결과: $secondResult")
                }


//                // ---- Enable notifications on RX
//                val notifOk = enableNotificationsInternal(g, rx, cccd)
//                if (!notifOk) {
//                    Log.d(TAG, "noti null")
//                    _connection.value = ConnectionState.Disconnected
//                    _services.value = ServiceState.Failed(-2)
//                    return
//                }
//                Log.d(TAG, "noti pass")

                // Ready (참고용 MTU: 네고 안 했으면 보통 23)
//                _services.value = ServiceState.Ready(23)

//                for (service in g.services) {
//                    val serviceUuid = service.uuid
//                    Log.i(TAG, "gattService : $serviceUuid")
//
//                    val characArray = arrayListOf<String>()
//                    for (characteristic in service.characteristics) {
//                        val characUuid = characteristic.uuid
//                        Log.i(TAG, "gattServiceCharac : $characUuid")
//                        characArray.add(characUuid.toString())
//                    }
//                }

            }

            override fun onCharacteristicChanged(
                g: BluetoothGatt,
                ch: BluetoothGattCharacteristic
            ) {

                if (ch.uuid != OtaGattSpec.GaiaResponse_UUID) return
                val responsePacket = ch.value ?: return
                _rx.tryEmit(responsePacket.copyOf())

                Log.v(TAG, "BLE 특성 변화 감지 : " + printLogBytesToString(responsePacket))

                if (responsePacket.size >= GAIA_HEADER_RESPONSE.size && responsePacket.copyOfRange(
                        0,
                        GAIA_HEADER_RESPONSE.size
                    ).contentEquals(GAIA_HEADER_RESPONSE)
                ) {

                    val packetHeader = byteExtractor(responsePacket[4])

                    when (packetHeader) {
                        HEADER_PASSWORD -> {
                            val packetSize = responsePacket.size
                            Log.w(TAG, "packetSize : $packetSize")
                            Log.w(
                                TAG,
                                "PacketInfo.PACKET_SIZE_PASSWORD : ${PacketInfo.PACKET_SIZE_PASSWORD}"
                            )
//                            if (packetSize != PacketInfo.PACKET_SIZE_PASSWORD) {
//                                Log.d(
//                                    TAG,
//                                    "BLE 특성 변경 감지 -> 보안코드 응답 패킷 사이즈 에러 : 사이즈 = $packetSize"
//                                )
//                                disconnect()
//                                return
//                            } else {
//                                if (byteExtractor(responsePacket[5]) == PacketInfo.PASSWORD_PASS) {
//                                    Log.d(TAG, "사용자의 내부기 키가 올바릅니다.")
//                                    Log.d(
//                                        TAG,
//                                        "내부기 키 인증에 성공했습니다. 상태정보 획득 시간초과 핸들러를 생성하고, 상태정보 획득 패킷을 전송합니다. "
//                                    )
//                                    // ===== 타임아웃 스타트 =====
//                                    startSoundProcessorTimeout {
//                                        Log.d(TAG, "보안코드 인증 시간초과입니다. 현재 연결된 기기와 연결해제합니다.")
//                                        disconnect()
//                                    }
//
//                                    // ===== 사운드 프로세서 생성 & 전송 =====
//                                    scope.launch {
//                                        val packet = packetMaker(
//                                            PacketInfo.HEADER_SOUND_PROCESSOR_INFO,
//                                            null,
//                                            1
//                                        )
//                                        val ok = writeOta(packet)
//                                        Log.d(
//                                            TAG,
//                                            "send soundProcessor packet result=$ok, len=${packet.size}"
//                                        )
//                                        if (!ok) {
//                                            cancelSoundProcessorTimeout()
//                                            disconnect()
//                                        }
//                                    }
//
//                                } else {
//                                    Log.d(TAG, "사용자의 내부기 키가 올바르지 않습니다.")
//                                    Log.d(TAG, "스캔을 멈추고, 사운드처리기와의 연결을 해제합니다.")
//                                    disconnect()
//                                }
//                            }
                        }

                        HEADER_SOUND_PROCESSOR_INFO -> {
                            val packetSize = responsePacket.size
                            Log.w(TAG, "packetSize : $packetSize")
                            Log.w(
                                TAG,
                                "PacketInfo.PACKET_SIZE_PROCESSOR_INFO : ${PacketInfo.PACKET_SIZE_PROCESSOR_INFO}"
                            )
                            if (packetSize != PacketInfo.PACKET_SIZE_PROCESSOR_INFO) {
                                Log.d(
                                    TAG,
                                    "BLE 특성 변경 감지 -> 사운드처리기 기기 및 맵 정보 읽기 패킷 사이즈 에러 : 사이즈 = $packetSize"
                                )
                                disconnect()
                                return
                            } else {
                                val fwVerLower = responsePacket[11]
                                val fwVerUpper = responsePacket[12]

                                for (i in responsePacket) {
                                    Log.v(
                                        TAG,
                                        "사운드처리기 responsePacket $i 은 '${i and 0xff.toByte()}' 입니다."
                                    )
                                }
                                Log.d(
                                    TAG,
                                    "사운드처리기 responsePacket은 '${responsePacket.toList()}' 입니다."
                                )
                                Log.d(TAG, "사운드처리기 펌웨어 버전은 '$fwVerUpper.$fwVerLower' 입니다.")

                                // ===== 타임아웃 스타트 =====
                                startPacketTimeout {
                                    Log.d(TAG, "내부기 시리얼 읽기 인증 시간초과입니다. 현재 연결된 기기와 연결해제합니다.")
                                    disconnect()
                                }

                                // ===== 사운드 프로세서 생성 & 전송 =====
                                scope.launch {
                                    val packet = packetMaker(
                                        PacketInfo.HEADER_AUDIO_INPUT_MAX_READ,
                                        null,
                                        1
                                    )
                                    val ok = writeOta(packet)
                                    Log.d(
                                        TAG,
                                        "send soundProcessor packet result=$ok, len=${packet.size}"
                                    )
                                    if (!ok) {
                                        cancelPacketTimeout()
                                        disconnect()
                                    }
                                }
                            }
                        }

                        HEADER_AUDIO_INPUT_MAX_READ -> {
                            val packetSize = responsePacket.size
                            Log.w(TAG, "packetSize : $packetSize")
                            Log.w(
                                TAG,
                                "PacketInfo.HEADER_AUDIO_INPUT_MAX_READ : ${PacketInfo.HEADER_AUDIO_INPUT_MAX_READ}"
                            )
                            if (packetSize < 6) {
                                Log.d(
                                    TAG,
                                    "BLE 특성 변경 감지 -> 내부기 시리얼 읽기 패킷 사이즈 에러 : 사이즈 = $packetSize"
                                )
                                disconnect()
                                return
                            } else { //--_internalSerial

                                val dataIndex = responsePacket[5].toInt()

                                when (dataIndex) {
                                    PacketInfo.SOUND_PRECESSING_PARAM_INDEX_MIN -> {


                                        if (packetSize != PacketInfo.PACKET_SIZE_SOUND_PROCESSING_PARAM_INDEX_1) {
                                            Log.d(
                                                TAG,
                                                "'사운드 신호처리 파라미터' 패킷 사이즈 에러. size=$packetSize"
                                            )
                                            disconnect()
                                            return
                                        }
                                        val p9 = responsePacket.getOrNull(9)?.toInt()?.and(0xFF)
                                            ?: run { Log.d(TAG, "no byte[9]"); return }
                                        val p10 = responsePacket.getOrNull(10)?.toInt()?.and(0xFF)
                                            ?: run { Log.d(TAG, "no byte[10]"); return }
                                        val p11 = responsePacket.getOrNull(11)?.toInt()?.and(0xFF)
                                            ?: run { Log.d(TAG, "no byte[11]"); return }
                                        val p12 = responsePacket.getOrNull(12)?.toInt()?.and(0xFF)
                                            ?: run { Log.d(TAG, "no byte[12]"); return }

                                        val isdID =
                                            (p9 shl 24) or (p10 shl 16) or (p11 shl 8) or p12

//                                            // 교체 매핑
//                                            isdID = when (isdID) {
//                                                0x1891001F -> {
//                                                    Log.d(TAG, "내부기 ID '#%08X' → '#1691000A'로 교체".format(isdID))
//                                                    0x1691000A
//                                                }
//                                                0x18910015 -> {
//                                                    Log.d(TAG, "내부기 ID '#%08X' → '#1691000B'로 교체".format(isdID))
//                                                    0x1691000B
//                                                }
//                                                0x1891000B -> {
//                                                    Log.d(TAG, "내부기 ID '#%08X' → '#1691000C'로 교체".format(isdID))
//                                                    0x1691000C
//                                                }
//                                                0x18910001 -> {
//                                                    Log.d(TAG, "내부기 ID '#%08X' → '#1691000D'로 교체".format(isdID))
//                                                    0x1691000D
//                                                }
//                                                0x1891001C -> {
//                                                    Log.d(TAG, "내부기 ID '#%08X' → '#1691000E'로 교체".format(isdID))
//                                                    0x1691000E
//                                                }
//                                                else -> isdID
//                                            }

                                        Log.d(
                                            TAG,
                                            "내부기 ID 읽기: ID = %s".format(
                                                String.format(
                                                    "#%08X",
                                                    isdID
                                                )
                                            )
                                        )

                                        _internalSerial.value = String.format("#%08X", isdID)

                                    }

                                    PacketInfo.SOUND_PRECESSING_PARAM_INDEX_MAX -> {
                                        // ===== 타임아웃 스타트 =====
                                        startPacketTimeout {
                                            Log.d(TAG, "사운드프로세서 쓰기 인증 시간초과입니다. 현재 연결된 기기와 연결해제합니다.")
                                            disconnect()
                                        }
                                        _connection.value = ConnectionState.Connected(g.device)

                                        // ===== 사운드 프로세서 생성 & 전송 =====
                                        scope.launch {
                                            val packet = packetMaker(
                                                HEADER_SOUND_PROCESSOR_STATUS,
                                                null,
                                                1
                                            )
                                            val ok = writeOta(packet)
                                            Log.d(
                                                TAG,
                                                "send HEADER_SOUND_PROCESSOR_STATUS packet result=$ok, len=${packet.size}"
                                            )
                                            if (!ok) {
                                                cancelPacketTimeout()
                                                disconnect()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HEADER_START_COMMAND -> {
                            // 타임아웃 취소
                            if (pendingCommand == OtaCommandType.START_COMMAND) {
                                cancelCommandTimeout()
                            }
                            Log.d(TAG,"HEADER_START_COMMAND Response : ${printLogBytesToString(responsePacket)}")
                            val rspCode = responsePacket[5]
                            when (rspCode) {
                                START_RESULT_ACCEPT -> {
                                    Log.d(TAG, "START COMMAND ACCEPT")
                                }

                                START_RESULT_ERR_EZAIRO_COMM -> {
                                    onErrorResponse(
                                        HEADER_START_COMMAND,
                                        rspCode,
                                        "START_RESULT_ERR_EZAIRO_COMM"
                                    )
                                    endCommandAtError()
                                    Log.e(TAG, "START COMMAND ERROR : START_RESULT_ERR_EZAIRO_COMM")
                                }

                                START_RESULT_ERR_LOW_BATTERY -> {
                                    onErrorResponse(
                                        HEADER_START_COMMAND,
                                        rspCode,
                                        "START_RESULT_ERR_LOW_BATTERY"
                                    )
                                    endCommandAtError()
                                    Log.e(TAG, "START COMMAND ERROR : START_RESULT_ERR_LOW_BATTERY")
                                }

                                START_RESULT_ERR_UNKNOWN -> {
                                    onErrorResponse(
                                        HEADER_START_COMMAND,
                                        rspCode,
                                        "START_RESULT_ERR_UNKNOWN"
                                    )
                                    endCommandAtError()
                                    Log.e(TAG, "START COMMAND ERROR : START_RESULT_ERR_UNKNOWN")
                                }
                            }

                            onBleResponse(HEADER_START_COMMAND, responsePacket[5], responsePacket)
                        }

                        HEADER_END_COMMAND -> {
                            // 타임아웃 취소
                            if (pendingCommand == OtaCommandType.END_COMMAND) {
                                cancelCommandTimeout()
                            }
                            Log.d(
                                TAG,
                                "HEADER_END_COMMAND : ${printLogBytesToString(responsePacket)}"
                            )
                            val rspCode = responsePacket[5]
                            when (rspCode) {
                                END_RESULT_ACCEPT -> {
                                    Log.d(TAG, "END COMMAND ACCEPT")
                                }

                                else -> {

                                    onErrorResponse(
                                        HEADER_END_COMMAND,
                                        rspCode,
                                        "END COMMAND ERROR"
                                    )
                                    Log.e(TAG, "END COMMAND ERROR")
                                }
                            }
                            onBleResponse(HEADER_END_COMMAND, rspCode, responsePacket)
                        }

                        HEADER_INFO_SELECT_COMMAND -> {
                            writeCurrentPacket(responsePacket)

                            Log.d(
                                TAG,
                                "┌HEADER_INFO_SELECT_COMMAND : ${
                                    printLogBytesToString(responsePacket)
                                }"
                            )
                            when (responsePacket[5]) {
                                INDEX_INFO_COMMAND -> { //0xC2 INFO_COMMAND
                                    val result = byteExtractor(responsePacket[6])
                                    when (result) {
                                        INFO_RESULT_ACCEPT -> {
                                            Log.d(TAG, "INFO COMMAND ACCEPT")
                                        }

                                        INFO_RESULT_REJECT_INVALID_SLOT_NUM -> {
                                            onErrorResponse(
                                                HEADER_INFO_SELECT_COMMAND,
                                                result,
                                                "INFO_RESULT_REJECT_INVALID_SLOT_NUM"
                                            )
                                            endCommandAtError()
                                            Log.e(
                                                TAG,
                                                "INFO COMMAND ERROR : REJECT_INVALID_SLOT_NUM"
                                            )
                                            disconnect()
                                        }

                                        INFO_RESULT_REJECT_CURRENT_BOOT_SLOT -> {
                                            onErrorResponse(
                                                HEADER_INFO_SELECT_COMMAND,
                                                result,
                                                "INFO_RESULT_REJECT_CURRENT_BOOT_SLOT"
                                            )
                                            endCommandAtError()
                                            Log.e(
                                                TAG,
                                                "INFO COMMAND ERROR : REJECT_CURRENT_BOOT_SLOT"
                                            )
                                            disconnect()
                                        }
                                    }
                                    val versionMajor = responsePacket[7]
                                    val versionMinor = responsePacket[8]
                                    val currentBootSlotNum = responsePacket[9]
                                    val preBootSlotNum = responsePacket[10]
                                    onInfoResponse(
                                        result.toInt(),
                                        versionMajor.toInt(),
                                        versionMinor.toInt(),
                                        currentBootSlotNum,
                                        preBootSlotNum
                                    )
                                    Log.d(
                                        TAG,
                                        "└INDEX_INFO_COMMAND : ${
                                            printLogBytesToString(responsePacket)
                                        }"
                                    )
                                }

                                INDEX_SELECT_COMMAND -> { //0xC2 SELECT_COMMAND
                                    val result = byteExtractor(responsePacket[6])
                                    when (result) {
                                        SELECT_RESULT_ACCEPT -> {
                                            Log.d(TAG, "SELECT COMMAND ACCEPT")

                                        }

                                        SELECT_RESULT_REJECT_INVALID_SLOT_NUM -> {
                                            onErrorResponse(
                                                HEADER_INFO_SELECT_COMMAND,
                                                result,
                                                "SELECT_RESULT_REJECT_INVALID_SLOT_NUM"
                                            )
                                            endCommandAtError()
                                            Log.e(
                                                TAG,
                                                "SELECT COMMAND ERROR : REJECT_INVALID_SLOT_NUM"
                                            )
                                            disconnect()
                                        }

                                        SELECT_RESULT_REJECT_CURRENT_BOOT_SLOT -> {
                                            onErrorResponse(
                                                HEADER_INFO_SELECT_COMMAND,
                                                result,
                                                "SELECT_RESULT_REJECT_CURRENT_BOOT_SLOT"
                                            )
                                            endCommandAtError()
                                            Log.e(
                                                TAG,
                                                "SELECT COMMAND ERROR : REJECT_CURRENT_BOOT_SLOT"
                                            )
                                            disconnect()
                                        }
                                    }
                                    onSelectResponse(result.toInt())
                                    Log.d(
                                        TAG,
                                        "└INDEX_SELECT_COMMAND : ${
                                            printLogBytesToString(responsePacket)
                                        }"
                                    )
                                }
                            }
                        }

                        HEADER_WRITE_COMMAND -> {
                            if (pendingCommand == OtaCommandType.DATA_HEADER ||
                                pendingCommand == OtaCommandType.DATA_WRITE) {
                                cancelCommandTimeout()
                            }
                            Log.d(
                                TAG,
                                "┌HEADER_WRITE_COMMAND : ${printLogBytesToString(responsePacket)}"
                            )
                            responsePacket[5]
                            responsePacket[6]
                            responsePacket[7]
                            val responseIndex: Int =
                                ((responsePacket[5].toInt() and 0xFF) shl 16) or
                                        ((responsePacket[6].toInt() and 0xFF) shl 8) or
                                        (responsePacket[7].toInt() and 0xFF)
                            when (responseIndex) {
                                INDEX_CHOOSE_FILE_HEADER_COMMAND -> {
                                    Log.d(
                                        TAG,
                                        "├INDEX_CHOOSE_FILE_HEADER_COMMAND : ${
                                            printLogBytesToString(responsePacket)
                                        }"
                                    )
                                    val rspCode = responsePacket[3 + 8]
                                    when (rspCode) {
                                        WRITE_0_RESULT_ACCEPT -> {
                                            Log.d(TAG, "WRITE_0_RESULT_ACCEPT")
                                        }

                                        WRITE_0_RESULT_INVALID_DATA_INDEX_ORDER -> {
                                            onErrorResponse(
                                                HEADER_WRITE_COMMAND,
                                                rspCode,
                                                "WRITE_0_RESULT_INVALID_DATA_INDEX_ORDER"
                                            )
                                            endCommandAtError()
                                            Log.d(TAG, "WRITE_0_RESULT_INVALID_DATA_INDEX_ORDER")
                                        }

                                        WRITE_0_RESULT_ERR_CURRENT_BOOT_SLOT -> {
                                            onErrorResponse(
                                                HEADER_WRITE_COMMAND,
                                                rspCode,
                                                "WRITE_0_RESULT_ERR_CURRENT_BOOT_SLOT"
                                            )
                                            endCommandAtError()
                                            Log.d(TAG, "WRITE_0_RESULT_ERR_CURRENT_BOOT_SLOT")
                                        }

                                        WRITE_0_RESULT_NO_FILE_EXIST -> {
                                            onErrorResponse(
                                                HEADER_WRITE_COMMAND,
                                                rspCode,
                                                "WRITE_0_RESULT_NO_FILE_EXIST"
                                            )
                                            endCommandAtError()
                                            Log.d(TAG, "WRITE_0_RESULT_NO_FILE_EXIST")
                                        }
                                    }
                                    onBleResponse(
                                        HEADER_WRITE_COMMAND,
                                        responsePacket[3 + 8],
                                        responsePacket
                                    )
                                }

                                else -> {
                                    Log.d(
                                        TAG,
                                        "├INDEX_WRITE_${responseIndex}_COMMAND : ${
                                            printLogBytesToString(responsePacket)
                                        }"
                                    )
                                    val rspCode = responsePacket[3 + 5]
                                    when (rspCode) {
                                        WRITE_RESULT_ACCEPT -> {
                                            Log.d(TAG, "WRITE_RESULT_ACCEPT")
                                        }

                                        WRITE_RESULT_INVALID_DATA_INDEX_ORDER -> {
                                            onErrorResponse(
                                                HEADER_WRITE_COMMAND,
                                                rspCode,
                                                "WRITE_RESULT_INVALID_DATA_INDEX_ORDER"
                                            )
                                            endCommandAtError()
                                            Log.d(TAG, "WRITE_RESULT_INVALID_DATA_INDEX_ORDER")
                                        }

                                        WRITE_RESULT_ERR_CURRENT_BOOT_SLOT -> {
                                            onErrorResponse(
                                                HEADER_WRITE_COMMAND,
                                                rspCode,
                                                "WRITE_RESULT_ERR_CURRENT_BOOT_SLOT"
                                            )
                                            endCommandAtError()
                                            Log.d(TAG, "WRITE_RESULT_ERR_CURRENT_BOOT_SLOT")

                                        }
                                    }
                                    onBleResponse(
                                        HEADER_WRITE_COMMAND,
                                        responsePacket[3 + 5],
                                        responsePacket
                                    )
                                }
                            }
                        }

                        HEADER_ERROR_COMMAND -> {
                            Log.d(
                                TAG,
                                "HEADER_ERROR_COMMAND : ${printLogBytesToString(responsePacket)}"
                            )
                            writeCurrentPacket(responsePacket)
                            val rspCode = responsePacket[5]
                            when (rspCode) {
                                ERROR_RESULT_NONE -> {
                                    onErrorResponse(
                                        HEADER_ERROR_COMMAND,
                                        rspCode,
                                        "ERROR_RESULT_NONE"
                                    )
                                    Log.d(TAG, "ERROR_RESULT_NONE")
                                }

                                ERROR_RESULT_EZAIRO_COMM_ERROR -> {
                                    onErrorResponse(
                                        HEADER_ERROR_COMMAND,
                                        rspCode,
                                        "ERROR_RESULT_EZAIRO_COMM_ERROR"
                                    )
                                    Log.d(TAG, "ERROR_RESULT_EZAIRO_COMM_ERROR")
                                }

                                ERROR_RESULT_LOW_BATTERY -> {
                                    onErrorResponse(
                                        HEADER_ERROR_COMMAND,
                                        rspCode,
                                        "ERROR_RESULT_LOW_BATTERY"
                                    )
                                    Log.d(TAG, "ERROR_RESULT_LOW_BATTERY")
                                }

                                ERROR_RESULT_NOT_OTA_MODE -> {
                                    onErrorResponse(
                                        HEADER_ERROR_COMMAND,
                                        rspCode,
                                        "ERROR_RESULT_NOT_OTA_MODE"
                                    )
                                    Log.d(TAG, "ERROR_RESULT_NOT_OTA_MODE")
                                }

                                ERROR_RESULT_INVALID_OTA_ORDER -> {
                                    onErrorResponse(
                                        HEADER_ERROR_COMMAND,
                                        rspCode,
                                        "ERROR_RESULT_INVALID_OTA_ORDER"
                                    )
                                    Log.d(TAG, "ERROR_RESULT_INVALID_OTA_ORDER")
                                }

                                ERROR_RESULT_UNKNOWN_ERROR -> {
                                    onErrorResponse(
                                        HEADER_ERROR_COMMAND,
                                        rspCode,
                                        "ERROR_RESULT_UNKNOWN_ERROR"
                                    )
                                    Log.d(TAG, "ERROR_RESULT_UNKNOWN_ERROR")
                                }
                            }
                            onBleResponse(HEADER_ERROR_COMMAND, responsePacket[5], responsePacket)

                            endCommandAtError()
                        }
                    }
                }

            }

            private fun endCommandAtError() {
                scope.launch {
                    val packet = packetMaker(HEADER_END_COMMAND, null, 1)
                    val ok = writeOta(packet)
                    Log.d(TAG, "send end command packet result=$ok, len=${packet.size}")
                    if (!ok) {
                        cancelPacketTimeout()
                        disconnect()
                    }
                }
            }


            override fun onDescriptorWrite(
                g: BluetoothGatt,
                d: BluetoothGattDescriptor,
                status: Int
            ) {
                if (d.uuid != OtaGattSpec.CCCD || otaRxCccd == null || d.characteristic?.uuid != otaRx?.uuid) return

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "CCCD enable OK → 시작: 패스키 타임아웃 + 패스키 패킷 전송")
                    _connection.value = ConnectionState.Connected(g.device)  // -> 실제로는 지워야함 현재 테스트 용도

                    val stabilizationDelay =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            500L
                        } else {
                            50L
                        }

                    // ===== 타임아웃 스타트 =====
//                    startPasswordTimeout {
//                        Log.d(TAG, "Password timeout. Disconnect.")
//                        disconnect()
//                    }

                    // ===== 패스키 생성 & 전송 =====
                    scope.launch {
                        delay(stabilizationDelay)
                        val key = passkeyProvider?.invoke()
                        if (key == null || key.isEmpty()) {
                            Log.e(TAG, "passkeyProvider가 null/empty를 반환. 인증 스킵.")
                            cancelPasswordTimeout()
                            return@launch
                        }
                        val packet =
                            buildPasswordPacket?.invoke(key) ?: key.toByteArray()
                        val ok = writeOta(packet)
                        Log.d(TAG, "send password packet result=$ok, len=${packet.size}")

                        if (!ok) {
                            cancelPasswordTimeout()
                            disconnect()
                        }
                    }
                } else {
                    Log.e(TAG, "CCCD write failed (status=$status)")
                    disconnect()
                }
            }
        }

        gatt = device.connectGatt(context, false, mGaiaGattCallback, BluetoothDevice.TRANSPORT_LE)

    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        Log.e(TAG, "gatt disconnect()")
//        cancelPasswordTimeout()
//        cancelSoundProcessorTimeout()
//        cancelPacketTimeout()
        cancelCommandTimeout()

        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _connection.value = ConnectionState.Disconnected
        _services.value = ServiceState.Idle
        _bond.value = BondState.None
        clearOtaHandles()
    }


    /** OTA 서비스/특성까지 확보되었는지 */
    fun isOtaReady(): Boolean = otaTx != null && otaRx != null && gatt != null

    /** 허용 페이로드 (MTU-3). 네고 안 했으면 보통 20. */
    fun maxPayload(): Int = ((23
            /**mtuSize**/
            ) - 3).coerceAtLeast(20)

    /** TX 특성에 쓰기 (payload는 maxPayload() 이하로 보낼 것) */
    @SuppressLint("MissingPermission")
    suspend fun writeOta(payload: ByteArray, noResponse: Boolean = true): Boolean {
//        delay(100L)

        Log.w(TAG, "gatt not exist")
        val g = gatt ?: return false
        Log.w(TAG, "gatt exist")
        val tx = otaTx ?: return false
        Log.w(TAG, "tx exist")
        if (payload.isEmpty()) return true
        Log.w(TAG, "payload : ${printLogBytesToString(payload)}")

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val rc =
                g.writeCharacteristic(tx, payload, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            when (rc) {
                BluetoothStatusCodes.SUCCESS -> {
                    Log.i(
                        TAG,
                        "[writeOta] enqueue OK (API33+). 최종 결과는 onCharacteristicWrite()에서 확인 필요"
                    )
                    true
                }
                else -> {
                    onErrorResponse(
                        (18  and 0xFF).toByte(),
                        (18  and 0xFF).toByte(),
                        "[writeOta] enqueue FAIL"
                    )
                    Log.e(TAG, "[writeOta] enqueue FAIL (API33+) rc=$rc (${statusToString(rc)})")
                    false
                }
            }
        } else {
            tx.value = payload
            tx.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val ok = g.writeCharacteristic(tx)
            if (!ok) {
                Log.e(TAG, "[writeOta] enqueue FAIL (<33). 보통 '바쁨' 또는 '잘못된 상태/프로퍼티'")
                onErrorResponse(
                    (18  and 0xFF).toByte(),
                    (18  and 0xFF).toByte(),
                    "[writeOta] enqueue FAIL"
                )
            } else {
                Log.i(TAG, "[writeOta] enqueue OK (<33). 최종 결과는 onCharacteristicWrite()에서 확인")
            }
            ok
        }
    }

    /** RX 알림 스트림: dev→app 수신 바이트 */
    fun notifications(): SharedFlow<ByteArray> = rx

    // ----- Internal -----

    private fun clearOtaHandles() {
        otaService = null; otaTx = null; otaRx = null; otaRxCccd = null
    }

    @SuppressLint("MissingPermission")
    private fun enableNotificationsInternal(
        g: BluetoothGatt,
        rx: BluetoothGattCharacteristic?,
        cccd: BluetoothGattDescriptor?
    ): Boolean {
        if (rx == null || cccd == null) return false
        val ok = g.setCharacteristicNotification(rx, true)
        if (!ok) return false

        val supportIndicate = rx.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
        val enableValue = if (supportIndicate)
            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        else
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

        cccd.value = enableValue
        return g.writeDescriptor(cccd)
    }

    // --- 사운드프로세스 정보 타임아웃
    private fun startPacketTimeout(onTimeout: () -> Unit) {
        cancelSoundProcessorTimeout()
        packetTimeoutJob = scope.launch {
            delay(packetTimeoutMs)
            onTimeout()
        }
    }

    private fun cancelPacketTimeout() {
        packetTimeoutJob?.cancel()
        packetTimeoutJob = null
    }

    // --- 사운드프로세스 정보 타임아웃
    private fun startSoundProcessorTimeout(onTimeout: () -> Unit) {
        cancelSoundProcessorTimeout()
        soundProcessorInfoTimeoutJob = scope.launch {
            delay(soundProcessorTimeoutMs)
            onTimeout()
        }
    }

    private fun cancelSoundProcessorTimeout() {
        soundProcessorInfoTimeoutJob?.cancel()
        soundProcessorInfoTimeoutJob = null
    }

    // --- 패스키 타임아웃
    private fun startPasswordTimeout(onTimeout: () -> Unit) {
        cancelPasswordTimeout()
        passwordTimeoutJob = scope.launch {
            delay(passwordTimeoutMs)
            onTimeout()
        }
    }

    private fun cancelPasswordTimeout() {
        passwordTimeoutJob?.cancel()
        passwordTimeoutJob = null
    }

    // --- Bond 브로드캐스트(시스템 페어링 UI 승인 후)
    private fun registerReceivers() {
        if (receiversRegistered) return
        Log.d(TAG, "액션을 registerReceivers")
        receiversRegistered = true
        context.registerReceiver(
            bondReceiver,
            makeIntentFilter()
        )
    }

    private fun makeIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) // 블루투스 자체에 대한 활성화/비활성화 이벤트
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) // 본딩 관련
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST) // 페어링 요청 관련
        return intentFilter
    }

    private fun unregisterReceivers() {
        if (!receiversRegistered) return
        receiversRegistered = false
        runCatching { context.unregisterReceiver(bondReceiver) }
    }

    private val bondReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context, intent: Intent) {
            val action = intent.action
            Log.d(TAG, "액션을 수신했습니다. -> $action")
            when (action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    Log.d(TAG, "device state. -> {${device.bondState}}")
                }
            }

        }
    }

    //C0 start command write
    suspend fun sendStartCommandByBle(): Boolean {
        val packet = packetMaker(HEADER_START_COMMAND, null, 1)
        Log.e(TAG, "sendStartCommandByBle : ${printLogBytesToString(packet)}")
        startCommandTimeout(OtaCommandType.START_COMMAND) {
            disconnect()
        }

        val result = writeOta(packet)
        if (!result) {
            cancelCommandTimeout()
        }
        return result
    }

    //C1 end command write
    suspend fun sendEndCommandByBle(): Boolean {
        val packet = packetMaker(HEADER_END_COMMAND, null, 1)
        Log.e(TAG, "sendEndCommandByBle : ${printLogBytesToString(packet)}")
        startCommandTimeout(OtaCommandType.END_COMMAND) {
            disconnect()
        }
        val result = writeOta(packet)
        if (!result) {
            cancelCommandTimeout()
        }
        return result
    }

    //C3 00 info command write
    suspend fun sendInfoCommandByBle(payload: ByteArray) {
//        val packet = c2InfoReq()
        val packet = packetMaker(HEADER_WRITE_COMMAND, payload, 1)
        Log.e(TAG, "sendInfoCommandByBle : ${printLogBytesToString(packet)}")
        writeOta(packet)
    }

    //C2 02 select command write
    suspend fun sendSelectCommandByBle(
        slot: Int,
    ) {
        val packet = c2Select(slot = slot)
        Log.e(TAG, "sendInfoCommandByBle : ${printLogBytesToString(packet)}")
        writeOta(packet)
    }

    //C3 00 write index 0 command write
    suspend fun sendWriteIndex0CommandByBle(payload: ByteArray): Boolean {
        val packet = packetMaker(HEADER_WRITE_COMMAND, payload, 18)
        Log.e(TAG, "sendWriteIndex0CommandByBle : ${printLogBytesToString(packet)}")
        startCommandTimeout(OtaCommandType.DATA_HEADER) {
            disconnect()
        }
        val result = writeOta(packet)
        if (!result) {
            cancelCommandTimeout()
        }
        return result
    }

    //C3 index write index 0 command write
    suspend fun sendWriteDataCommandByBle(payload: ByteArray): Boolean {
        val packet = packetMaker(HEADER_WRITE_COMMAND, payload, 20)
        Log.e(TAG, "sendWriteDataCommandByBle : ${printLogBytesToString(packet)}")
        startCommandTimeout(OtaCommandType.DATA_WRITE) {
            disconnect()
        }
        val result = writeOta(packet)
        if (!result) {
            cancelCommandTimeout()
        }
        return result
    }

    private fun statusToString(code: Int): String = when (code) {
        BluetoothStatusCodes.SUCCESS -> "SUCCESS"
        BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED -> "WRITE_NOT_ALLOWED"
        BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY -> "ERROR_GATT_WRITE_REQUEST_BUSY"
        BluetoothStatusCodes.ERROR_PROFILE_SERVICE_NOT_BOUND -> "SERVICE_NOT_BOUND"
        BluetoothStatusCodes.ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION -> "NO_BT_CONNECT_PERMISSION"
        BluetoothStatusCodes.ERROR_UNKNOWN -> "UNKNOWN"
        else -> "CODE_$code"
    }

    private var pendingCommand: OtaCommandType? = null

    // 명령 타임아웃 시작
    private fun startCommandTimeout(commandType: OtaCommandType, onTimeout: () -> Unit) {
        cancelCommandTimeout()
        pendingCommand = commandType
        commandTimeoutJob = scope.launch {
            delay(commandTimeoutMs)
            Log.e(TAG, "${commandType.name} 명령 응답 타임아웃 (${commandTimeoutMs}밀리초). 연결을 해제합니다.")
            onTimeout()
        }
    }

    // 명령 타임아웃 취소
    private fun cancelCommandTimeout() {
        commandTimeoutJob?.cancel()
        commandTimeoutJob = null
        pendingCommand = null
    }
}

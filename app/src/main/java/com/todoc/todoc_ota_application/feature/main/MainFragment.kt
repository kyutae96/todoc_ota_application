package com.todoc.todoc_ota_application.feature.main

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.BLUE
import android.graphics.Color.RED
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.internal.ThemeEnforcement.obtainStyledAttributes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import com.todoc.todoc_ota_application.R

import com.todoc.todoc_ota_application.core.model.OtaFileType
import com.todoc.todoc_ota_application.core.model.OtaPreparedFile
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.packetMaker
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.printLogBytesToString
import com.todoc.todoc_ota_application.core.proto.PacketBuilder.toHex
import com.todoc.todoc_ota_application.data.ble.BleConnector
import com.todoc.todoc_ota_application.data.ble.ConnectionState
import com.todoc.todoc_ota_application.data.ble.PacketInfo
import com.todoc.todoc_ota_application.data.ble.PacketInfo.HEADER_ERROR_COMMAND
import com.todoc.todoc_ota_application.data.ble.PacketInfo.INFO_RESULT_ACCEPT
import com.todoc.todoc_ota_application.data.ble.ScanningState
import com.todoc.todoc_ota_application.databinding.FragmentMainBinding
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import com.todoc.todoc_ota_application.feature.permission.PermissionHelper
import com.todoc.todoc_ota_application.startup.AuthGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**Manual update**/
class MainFragment : Fragment(R.layout.fragment_main) {
    private val vm: MainViewModel by activityViewModels()
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var scanDialog: AlertDialog? = null
    private var firebaseDataDialog: AlertDialog? = null
    private var scanCollectJob: Job? = null
    private var firebaseDataCollectJob: Job? = null
    private val collectPercent = IntArray(5) // 1..4 사용
    private var allDoneFired = false
    private var wasAll100 = false
    private val TAG = this.javaClass.simpleName
    private var bootSlotVersion: String? = "---"

    @SuppressLint("MissingPermission")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        val nav = findNavController()
        val hasPerm = PermissionHelper.hasAllPermissions(requireContext())
        if (!hasPerm) {
            nav.navigate(R.id.action_startup_to_permission); return
        }
        val bind = FragmentMainBinding.bind(v)
        bindViews(bind)
        bindButtons(bind, nav)
        collectFlows(bind)


//        scanAndConnectByName()


    }

    private fun bindViews(bind: FragmentMainBinding) {
        bind.btnRunAll.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.selector_btn_bg)


//        bind.btnHelp.tooltipText = "여기가 도움말 버튼입니다."
//        TooltipCompat.setTooltipText(bind.btnHelp, "여기가 도움말 버튼입니다.")
    }

    private fun bindButtons(bind: FragmentMainBinding, nav: NavController) {
        bind.deleteLogBtn.setOnClickListener {
            vm.clearPackets()
        }
        bind.btnHelp.setOnClickListener {
            showTooltip(
                it,
                "Bootloader 버전 : ${bootSlotVersion}\n현재 슬롯은 현재 동작중인 슬롯입니다.\n이전 슬롯은 이전에 동작했던 슬롯입니다.\n최신 슬롯은 최근 write를 진행한 슬롯입니다."
            )
        }

        bind.backToVersionBtn.setOnClickListener {
            lifecycleScope.launch {
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        vm.finishOtaSession(
                            status = "disconnect",
                            errorCode = "backToLoginBtn"
                        )
                    }.onFailure { e -> Log.e(TAG, "finishOtaSession disconnect", e) }
                }
//                val repo = LocalAuthRepository(requireContext())
//                repo.clearAll()
                nav.navigate(R.id.action_main_to_version)
            }
        }

        // OTA 쓰기 시작 버튼
        bind.btnRunAll.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val ui = vm.buildSlotUiParamsForCurrentDevice()
                showSlotBottomSheetAndRun(
                    bind = bind,
                    currentBootSlot = ui.currentBootSlot,
                    slot1Date = ui.slot1Date,
                    slot2Date = ui.slot2Date,
                    isSlot1RecentUpdated = ui.isSlot1RecentUpdated,
                    isSlot2RecentUpdated = ui.isSlot2RecentUpdated
                )
            }
        }


        bind.scanBtn.setOnClickListener {
            Log.w(TAG, "START_SCAN")
            Toast.makeText(requireContext(), "스캔 시작", Toast.LENGTH_SHORT).show()
            showScanDialog()
        }
        bind.startCommand.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_options, null)

            val rgControlType = dialogView.findViewById<RadioGroup>(R.id.rgControlType)
            val rgTarget = dialogView.findViewById<RadioGroup>(R.id.rgTarget)

            AlertDialog.Builder(requireContext())
                .setTitle("Control OP mode")
                .setView(dialogView)
                .setPositiveButton("확인") { dialog, _ ->
                    val selectedControlType = when(rgControlType.checkedRadioButtonId) {
                        R.id.rbControlType1 -> 0x00
                        R.id.rbControlType2 -> 0x01
                        else -> null
                    }
                    val selectedTarget = when(rgTarget.checkedRadioButtonId) {
                        R.id.rbTarget1 -> 0x00
                        R.id.rbTarget2 -> 0x01
                        R.id.rbTarget3 -> 0x02
                        R.id.rbTarget4 -> 0x03
                        else -> null
                    }

                    if (selectedControlType == 0x00 && selectedTarget == null){
                        viewLifecycleOwner.lifecycleScope.launch {
                            Log.w(TAG, "startCommand")
                            if (!vm.connector.isOtaReady()) {
                                Log.w(TAG, "GATT 준비 전입니다(서비스/CCCD 미완).")
                                return@launch
                            }
                            val ok = vm.sendStartCommand(selectedControlType.toByte(), 0x00.toByte())
                            Log.d(TAG, "send HEADER_START_COMMAND result=$ok")
                        }
                        dialog.dismiss()
                    }else if (selectedControlType != null && selectedTarget != null) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            Log.w(TAG, "startCommand")
                            if (!vm.connector.isOtaReady()) {
                                Log.w(TAG, "GATT 준비 전입니다(서비스/CCCD 미완).")
                                return@launch
                            }
                            val ok = vm.sendStartCommand(selectedControlType.toByte(), selectedTarget.toByte())
                            Log.d(TAG, "send HEADER_START_COMMAND result=$ok")
                        }
                        dialog.dismiss()

                    } else {
                        Toast.makeText(requireContext(), "모든 옵션을 선택해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        bind.endCommand.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_target, null)

            val rgControlType = dialogView.findViewById<RadioGroup>(R.id.rgTargetType)

            AlertDialog.Builder(requireContext())
                .setTitle("End OP mode")
                .setView(dialogView)
                .setPositiveButton("확인") { dialog, _ ->
                    val selectedControlType = when(rgControlType.checkedRadioButtonId) {
                        R.id.rbTarget1 -> 0x00
                        R.id.rbTarget2 -> 0x01
                        R.id.rbTarget3 -> 0x02
                        R.id.rbTarget4 -> 0x03
                        else -> null
                    }
                    if (selectedControlType != null) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            Log.w(TAG, "endCommand")
                            if (!vm.connector.isOtaReady()) {
                                Log.w(TAG, "GATT 준비 전입니다(서비스/CCCD 미완).")
                                return@launch
                            }
                            val ok = vm.sendEndCommand(selectedControlType.toByte())
                            Log.d(TAG, "send HEADER_END_COMMAND result=$ok")
                        }
                        dialog.dismiss()

                    } else {
                        Toast.makeText(requireContext(), "모든 옵션을 선택해주세요", Toast.LENGTH_SHORT).show()
                    }

                }
                .setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        bind.infoCommand.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                Log.w(TAG, "infoCommand")
                if (!vm.connector.isOtaReady()) {
                    Log.w(TAG, "GATT 준비 전입니다(서비스/CCCD 미완).")
                    return@launch
                }
                val ok = vm.sendInfoCommand()
                Log.d(TAG, "send HEADER_INFO_SELECT_COMMAND result=$ok")
            }

        }
        bind.selectCommand.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                Log.w(TAG, "selectCommand")
                if (!vm.connector.isOtaReady()) {
                    Log.w(TAG, "GATT 준비 전입니다(서비스/CCCD 미완).")
                    return@launch
                }
                val ui = vm.buildBootSlotUiParamsForCurrentDevice()
                showBootBottomSheetAndRun(
                    bind = bind,
                    currentBootSlot = ui.currentBootSlot,
                    slot1Date = ui.slot1Date,
                    slot2Date = ui.slot2Date,
                    isSlot1RecentUpdated = ui.isSlot1RecentUpdated,
                    isSlot2RecentUpdated = ui.isSlot2RecentUpdated
                )
            }
        }

        bind.resetCommand.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                Log.w(TAG, "resetCommand")
                if (!vm.connector.isOtaReady()) {
                    Log.w(TAG, "GATT 준비 전입니다(서비스/CCCD 미완).")
                    return@launch
                }

                val ok = vm.sendResetCommand()
                Log.d(TAG, "send HEADER_RESET_COMMAND result=$ok")
            }

        }


        bind.emptyFileLayout.setOnClickListener {
            showFirebaseDataDialogMulti(bind) { files, folderName ->
                Log.w(TAG, "files : ${files}")
                Log.w(TAG, "folderName : ${folderName}")
                val version = folderName        // UI에서 입력받아 사용
                val base = "OTA/$version"

                collectPercent.fill(0)
                vm.downloadDataToFile(base)

            }
        }



        bind.changeVersion.setOnClickListener {
            showFirebaseDataDialogMulti(bind) { files, folderName ->
                Log.w(TAG, "files : ${files}")
                Log.w(TAG, "folderName : ${folderName}")
                val version = folderName        // UI에서 입력받아 사용
                val base = "OTA/$version"

                collectPercent.fill(0)
                vm.downloadDataToFile(base)

            }
        }

        var isProgrammatic = false
        val children = listOf(
            bind.mfstCheckBox,
            bind.app0CheckBox,
            bind.app1CheckBox,
            bind.app2CheckBox
        )

        bind.selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammatic) return@setOnCheckedChangeListener
            isProgrammatic = true
            children.forEach { it.isChecked = isChecked }
            isProgrammatic = false
        }

        children.forEach { cb ->
            cb.setOnCheckedChangeListener { _, _ ->
                if (isProgrammatic) return@setOnCheckedChangeListener
                isProgrammatic = true
                bind.selectAllCheckBox.isChecked = children.all { it.isChecked }
                isProgrammatic = false
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun collectFlows(bind: FragmentMainBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                //스캔 상태 확인
                launch {
                    vm.scanning.collectLatest { st ->
                        when (st) {
                            is ScanningState.NotScanning -> {
                                Log.w(TAG, "NotScanning")
                                bind.tvDeviceName.text = "연결 없음 / 스캔 시작"
//                        bind.btnRunAll.visibility = View.GONE
                            }

                            is ScanningState.Scanning -> {
                                Log.w(TAG, "Scanning")
                                bind.tvDeviceName.text = "스캔 진행중..."
//                        bind.btnRunAll.visibility = View.GONE
                            }
                        }
                    }
                }
                //연결 상태 확인
                launch {
                    vm.connection.collectLatest { st ->
                        when (st) {
                            is ConnectionState.Connected -> {
                                Log.w(TAG, "Connected")
                                bind.tvDeviceName.text =
                                    st.device.name ?: st.device.address ?: "알 수 없음"
                                bind.btnRunAll.isEnabled = true
                                setDot(bind.viewStatusDot, BLUE)
                                LocalAuthRepository(requireContext()).savePairingDevice(st.device.name)
                                dismissScanDialog()
                            }

                            ConnectionState.Connecting -> {
                                Log.w(TAG, "Connecting")
                                bind.tvDeviceName.text = "페어링 진행중..."
//                        bind.btnRunAll.visibility = View.GONE
                                setDot(bind.viewStatusDot, Color.parseColor("#FFA000"))
                            }

                            ConnectionState.Disconnected -> {
                                Log.w(TAG, "Disconnected")
//                        bind.btnRunAll.visibility = View.GONE
                                bind.tvDeviceName.text = "연결 없음 / 스캔 시작"
                                setDot(bind.viewStatusDot, RED)
                            }
                        }
                    }
                }
                //내부기 시리얼 확인
                launch {
                    vm.internalSerial.collectLatest { serial ->
                        bind.tvDeviceSerial.text = "#$serial"
                    }
                }
                //Ready될 때만 버튼 활성화
//                launch {
//                    vm.ready.collect { isReady ->
//                        bind.startCommand.isEnabled = isReady;bind.endCommand.isEnabled = isReady
//                    }
//                }
                // 로그 확인용
                launch {
                    vm.packetFlow.collect { packets: List<ByteArray> ->
                        bind.packetCommand.text =
                            packets.joinToString("\n") { printLogBytesToString(it) }
                        bind.packetCommands.text =
                            packets.takeLast(4).joinToString("\n") { printLogBytesToString(it) }
                    }
                }
                /**COLLECT**/
                launch {
                    vm.progress.collect { p ->
                        if (p == null) return@collect
                        val pct = p.percent.coerceIn(0, 100)

                        fun showDeterminate(
                            v: com.google.android.material.progressindicator.LinearProgressIndicator,
                            tvPercent: TextView,
                            tvSize: TextView
                        ) {
                            v.isIndeterminate = false
                            v.max = 100
                            v.setProgressCompat(pct, /*animated=*/true)
                            tvSize.text = p.downloaded.toString() // 필요하면 humanBytes로 교체
                            if (pct >= 100) {
                                v.visibility = View.GONE
                                tvPercent.text = "다운로드 완료"
                                tvPercent.setTextColor(Color.parseColor("#29CC6A"))
                            } else {
                                v.visibility = View.VISIBLE
                                tvPercent.text = "$pct%"
                                tvPercent.setTextColor(Color.parseColor("#999999"))
                            }
                        }

                        when (p.fileNum) {
                            1 -> showDeterminate(
                                bind.mfstCollectProgress,
                                bind.mfstCollectPercentTextview,
                                bind.mfstCollectSizeTextview
                            )

                            2 -> showDeterminate(
                                bind.app0CollectProgress,
                                bind.app0CollectPercentTextview,
                                bind.app0CollectSizeTextview
                            )

                            3 -> showDeterminate(
                                bind.app1CollectProgress,
                                bind.app1CollectPercentTextview,
                                bind.app1CollectSizeTextview
                            )

                            4 -> showDeterminate(
                                bind.app2CollectProgress,
                                bind.app2CollectPercentTextview,
                                bind.app2CollectSizeTextview
                            )
                        }

                        collectPercent[p.fileNum] = pct
                        maybeFireAllCollected(bind)

                        Log.d(TAG, "P${p.fileNum} d=${p.downloaded} total=${p.total} pct=$pct")
                    }
                }

                /**ERROR 응답**/
                launch {
                    vm.errorResponseFlow.collect { rsp ->
                        Toast.makeText(requireContext(), rsp.message, Toast.LENGTH_SHORT).show()
                        vm.disconnect(rsp.message)
                    }
                }

                /**write C3 응답**/
                launch {
                    vm.bleResponseFlow.collect { r ->
                        vm.onOtaResponse(r.header, r.commandId)
                        if (r.header == HEADER_ERROR_COMMAND) {
                            Log.e(TAG, "ERROR_COMMAND : ${r.commandId.toHex()}")
                            runCatching {
                                vm.finishOtaSession(
                                    status = "ERROR_COMMAND",
                                    errorCode = r.commandId.toHex()
                                )
                            }.onFailure { e -> Log.e(TAG, "finishOtaSession failed", e) }
                            return@collect
                        }
                    }
                }

                /**info C2 응답**/
                launch {
                    vm.infoState.filterNotNull().collect { response ->
                        if (response.result.toByte() == INFO_RESULT_ACCEPT) {
                            bind.preSlotTxt.text = response.preBootSlotNum.toHex()
                            bind.currentSlotTxt.text = response.currentBootSlotNum.toHex()
                            bootSlotVersion =
                                "${response.versionMajor}.${response.versionMinor}"

                            Log.e(TAG, "result error : ${response.result}")
                        }
                    }
                }

                /**select C2 응답**/
                launch {
                    vm.selectState.filterNotNull().collect { response ->
                        if (response.toByte() == INFO_RESULT_ACCEPT) {
                            Log.e(TAG, "result Accept : ${response}")
                            runCatching {
                                vm.selectOtaSession(
                                    status = "SELECT_SUCCESS",
                                    slotSelected = response.toInt(),
                                    sessionType = "INFO_RESULT_ACCEPT"
                                )
                            }.onFailure { e -> Log.e(TAG, "selectOtaSession failed", e) }
                        } else {
                            Log.e(TAG, "result error : ${response}")
                            runCatching {
                                vm.selectOtaSession(
                                    status = "ERROR_SELECT",
                                    errorCode = response.toInt().toString(),
                                    slotSelected = response.toInt(),
                                    sessionType = "ERROR_SELECT"
                                )
                            }.onFailure { e -> Log.e(TAG, "selectOtaSession failed", e) }
                        }
                    }
                }
                /**local에서 파일 받기**/
                launch {
                    prefillCollectFromLocal(bind)
                }
//                        launch {
//                vm.otaFullProgress.filterNotNull().collect { response ->
//                    val tvLeftPercentTime =
//                        bind.tabSwitchPanel.findViewById<TextView>(R.id.tvLeftPercentTime)
//                    tvLeftPercentTime.text = response
//                }
//        }
                launch {
                    vm.preparedPlan.collectLatest { plan ->
                        if (plan == null) return@collectLatest

                        val layoutManifest = bind.layoutManifest
                        val layoutApp0 = bind.layoutApp0
                        val layoutApp1 = bind.layoutApp1
                        val layoutApp2 = bind.layoutApp2

                        val tvHeaderManifest = bind.tvHeaderManifest
                        val tvHeaderApp0 = bind.tvHeaderApp0
                        val tvHeaderApp1 = bind.tvHeaderApp1
                        val tvHeaderApp2 = bind.tvHeaderApp2

                        val pgHeaderManifest = bind.pgHeaderManifest
                        val pgHeaderApp0 = bind.pgHeaderApp0
                        val pgHeaderApp1 = bind.pgHeaderApp1
                        val pgHeaderApp2 = bind.pgHeaderApp2

                        val tvHeaderManifestSize = bind.tvHeaderManifestSize
                        val tvHeaderApp0Size = bind.tvHeaderApp0Size
                        val tvHeaderApp1Size = bind.tvHeaderApp1Size
                        val tvHeaderApp2Size = bind.tvHeaderApp2Size

                        val tvHeaderManifestPercent = bind.tvHeaderManifestPercent
                        val tvHeaderApp0Percent = bind.tvHeaderApp0Percent
                        val tvHeaderApp1Percent = bind.tvHeaderApp1Percent
                        val tvHeaderApp2Percent = bind.tvHeaderApp2Percent


                        val mf = plan.files.find { it.type == OtaFileType.MANIFEST }
                        val a0 = plan.files.find { it.type == OtaFileType.APP000 }
                        val a1 = plan.files.find { it.type == OtaFileType.APP001 }
                        val a2 = plan.files.find { it.type == OtaFileType.APP002 }

                        fun initSection(
                            file: OtaPreparedFile?,
                            layout: LinearLayout,
                            title: TextView,
                            progress: ProgressBar,
                            size: TextView,
                            pct: TextView,
                            label: String
                        ) {
                            if (file != null) {
                                layout.visibility = View.VISIBLE
                                title.text = label
                                size.text = humanBytes(file.length)
                                progress.progress = 0
                                pct.text = percentText(0, 0, file.endIndexNum)
                            } else {
                                layout.visibility = View.GONE
                            }
                        }

                        initSection(
                            mf,
                            layoutManifest,
                            tvHeaderManifest,
                            pgHeaderManifest,
                            tvHeaderManifestSize,
                            tvHeaderManifestPercent,
                            "MANIFEST"
                        )
                        initSection(
                            a0,
                            layoutApp0,
                            tvHeaderApp0,
                            pgHeaderApp0,
                            tvHeaderApp0Size,
                            tvHeaderApp0Percent,
                            "APP000"
                        )
                        initSection(
                            a1,
                            layoutApp1,
                            tvHeaderApp1,
                            pgHeaderApp1,
                            tvHeaderApp1Size,
                            tvHeaderApp1Percent,
                            "APP001"
                        )
                        initSection(
                            a2,
                            layoutApp2,
                            tvHeaderApp2,
                            pgHeaderApp2,
                            tvHeaderApp2Size,
                            tvHeaderApp2Percent,
                            "APP002"
                        )
                    }
                }

                launch {
                    vm.otaFileProgress.collect { p ->
                        if (p == null) return@collect

                        val pgHeaderManifest = bind.pgHeaderManifest
                        val pgHeaderApp0 = bind.pgHeaderApp0
                        val pgHeaderApp1 = bind.pgHeaderApp1
                        val pgHeaderApp2 = bind.pgHeaderApp2

                        val tvManifest = bind.tvHeaderManifestPercent
                        val tvApp0 = bind.tvHeaderApp0Percent
                        val tvApp1 = bind.tvHeaderApp1Percent
                        val tvApp2 = bind.tvHeaderApp2Percent


                        when (p.fileId.uppercase()) {
                            "MANIFEST" -> {
                                tvManifest?.text =
                                    percentText(p.percent, p.processedChunks, p.totalChunks)
                                pgHeaderManifest.progress = p.percent
                            }

                            "APP000" -> {
                                tvApp0?.text =
                                    percentText(p.percent, p.processedChunks, p.totalChunks)
                                pgHeaderApp0.progress = p.percent
                            }

                            "APP001" -> {
                                tvApp1?.text =
                                    percentText(p.percent, p.processedChunks, p.totalChunks)
                                pgHeaderApp1.progress = p.percent
                            }

                            "APP002" -> {
                                tvApp2?.text =
                                    percentText(p.percent, p.processedChunks, p.totalChunks)
                                pgHeaderApp2.progress = p.percent
                            }
                        }

                        if (p.percent % 5 == 0) {
                            launch {
                                kotlin.runCatching {
                                    vm.logEvent(
                                        type = "progress",
                                        fileId = p.fileId,
                                        percent = p.percent,
                                        processedChunks = p.processedChunks,
                                        totalChunks = p.totalChunks,
                                        message = "progress -> fileId : ${p.fileId}, percent : ${p.percent}, processedChunks : ${p.processedChunks}, totalChunks : ${p.totalChunks}"
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }


    private fun scanAndConnectByName() {
        // 2) 자동 연결: "TD_L_${initial}_FFFF"
        viewLifecycleOwner.lifecycleScope.launch {
            val repo = LocalAuthRepository(requireContext())
            val initial = repo.getInitialOrNull()
            if (!initial.isNullOrBlank()) {
                val expected = "TD_L_${initial}_FFFF"
                vm.tryAutoConnectByName(expected, timeoutMs = 6000) {
                    // 타임아웃 → 스캔 다이얼로그로 수동 선택
                    if (scanDialog?.isShowing != true) showScanDialog()
                }
            } else {
                // initial 없으면 바로 수동 선택
                showScanDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        if (scanDialog?.isShowing == true) vm.startScan()
    }

    override fun onPause() {
        super.onPause()
//        vm.stopScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.disconnect("FragmentMain onDestroy")
    }

    @SuppressLint("MissingPermission")
    private fun showScanDialog() {
        val ctx = requireContext()

        val recycler = RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
        }
        val adapter = DeviceAdapter { item ->
            viewLifecycleOwner.lifecycleScope.launch {
                val target = vm.devices.value.firstOrNull { it.device.address == item.address }
                if (target != null) {
                    vm.connectTo(target)
                    scanDialog?.dismiss()
                }
            }
        }
        recycler.adapter = adapter

        scanDialog = AlertDialog.Builder(ctx)
            .setTitle("블루투스 장치 선택")
            .setView(recycler)
            .setOnDismissListener { vm.stopScan(); scanCollectJob?.cancel() }
            .create()

        scanCollectJob?.cancel()
        scanCollectJob = viewLifecycleOwner.lifecycleScope.launch {
            vm.devices
                .sample(500)
                .map { results ->
                    results
                        .distinctBy { it.device.address }
                        .filter { it.rssi > -90 }
                        .sortedByDescending { it.rssi }
                        .take(50)
                        .map {
                            DeviceItem(
                                address = it.device.address ?: "",
                                name = it.device.name ?: it.device.address ?: "알 수 없음",
                                rssi = it.rssi
                            )
                        }
                }
                .distinctUntilChanged()
                .collectLatest { items ->
                    adapter.submitList(items)
                }
        }

        scanDialog?.show()
        vm.startScan()
    }


    private fun dismissScanDialog() {
        scanCollectJob?.cancel()
        scanDialog?.dismiss()
        scanDialog = null
    }

    private fun showFirebaseDataDialogMulti(
        bind: FragmentMainBinding,
        onDownload: (List<DisplayRow>, String?) -> Unit = { _, _ -> }
    ) {
        val ctx = requireContext()

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 16)
        }
        val header = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val tvPath = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            textSize = 16f
        }
        val btnUp = Button(ctx).apply { text = "상위"; isEnabled = false }
        header.addView(tvPath); header.addView(btnUp)

        val listView = ListView(ctx).apply {
            dividerHeight = 1
            choiceMode = ListView.CHOICE_MODE_MULTIPLE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val btnDownload = Button(ctx).apply {
            text = "다운로드"
            isEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(header)
        container.addView(listView)
        container.addView(btnDownload)

        scanDialog = AlertDialog.Builder(ctx)
            .setTitle("Firebase Storage")
            .setView(container)
            .setNegativeButton("닫기", null)
            .setOnDismissListener { firebaseDataCollectJob?.cancel() }
            .create()

        /**상태**/
        val rows = mutableListOf<DisplayRow>()
        val selectedFilePositions = mutableSetOf<Int>()  //  파일 다중 선택
        var selectedFolderPos: Int? = null               // 폴더 단일 선택
        var currentPath = "OTA"
        val pathStack = mutableListOf<String>()          // 상위 이동

        val defaultTextColor = Color.BLACK
        val selectedBg = Color.parseColor("#2962FF")
        val selectedFg = Color.WHITE

        /**공용 유틸**/
        suspend fun toDisplayRowFile(ref: com.google.firebase.storage.StorageReference): DisplayRow {
            val meta = runCatching { ref.metadata.await() }.getOrNull()
            val url = runCatching { ref.downloadUrl.await() }.getOrNull()?.toString().orEmpty()
            val size = meta?.sizeBytes ?: -1L
            val timeMs = meta?.updatedTimeMillis ?: 0L
            val df =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .apply {
                        timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    }
            val timeStr = if (timeMs > 0) df.format(Date(timeMs)) else "unknown"
            val sizeStr =
                if (size >= 0) Formatter.formatFileSize(ctx, size) else "?"
            return DisplayRow(ref.name, false, timeStr, sizeStr, url, ref)
        }

        suspend fun listFilesRecursive(prefix: com.google.firebase.storage.StorageReference): List<com.google.firebase.storage.StorageReference> {
            val out = mutableListOf<com.google.firebase.storage.StorageReference>()
            val stack = ArrayDeque<com.google.firebase.storage.StorageReference>()
            stack.add(prefix)
            while (stack.isNotEmpty()) {
                val cur = stack.removeLast()
                val res = cur.listAll().await()
                out += res.items
                res.prefixes.forEach { stack.add(it) }
            }
            return out
        }

        fun refreshButtons() {
            tvPath.text = "/$currentPath"
            btnUp.isEnabled = pathStack.isNotEmpty()
            btnDownload.isEnabled = selectedFolderPos != null || selectedFilePositions.isNotEmpty()
        }

        val adapter = object : ArrayAdapter<DisplayRow>(ctx, 0, rows) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = (convertView as? TextView) ?: TextView(ctx).apply {
                    setPadding(24, 24, 24, 24)
                    setTextIsSelectable(false)
                    setLines(3)
                }
                val item = getItem(position)!!
                tv.text = if (item.isFolder) {
                    "📁 ${item.name}\n(폴더)"
                } else {
                    "${item.name}\n업로드: ${item.timeStr} • ${item.sizeStr}\n${item.url}"
                }

                val isSelected = if (item.isFolder)
                    (selectedFolderPos == position)
                else
                    (position in selectedFilePositions)

                if (isSelected) {
                    tv.setBackgroundColor(selectedBg)
                    tv.setTextColor(selectedFg)
                } else {
                    tv.setBackgroundColor(Color.TRANSPARENT)
                    tv.setTextColor(defaultTextColor)
                }
                return tv
            }
        }
        listView.adapter = adapter

        fun loadPath(path: String) {
            firebaseDataCollectJob?.cancel()
            firebaseDataCollectJob = viewLifecycleOwner.lifecycleScope.launch {
                // 경로 바뀌면 선택 초기화
                selectedFilePositions.clear()
                selectedFolderPos = null
                btnDownload.isEnabled = false
                rows.clear()
                adapter.notifyDataSetChanged()
                refreshButtons()

                try {
                    val storage = Firebase.storage
                    val dirRef = storage.reference.child(path)
                    val listResult = dirRef.listAll().await()

                    val folderRows = listResult.prefixes
                        .sortedBy { it.name.lowercase() }
                        .map { DisplayRow(it.name, true, ref = it) }

                    val fileRows = withContext(Dispatchers.IO) {
                        listResult.items.map { ref ->
                            async { toDisplayRowFile(ref) }
                        }.awaitAll().sortedBy { it.name.lowercase() }
                    }

                    rows.addAll(folderRows)
                    rows.addAll(fileRows)
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    rows.clear()
                    rows.add(
                        DisplayRow(
                            name = "불러오기 실패",
                            isFolder = false,
                            timeStr = e.localizedMessage ?: e.javaClass.simpleName,
                            sizeStr = "-",
                            url = "",
                            ref = Firebase.storage.reference
                        )
                    )
                    adapter.notifyDataSetChanged()
                } finally {
                    refreshButtons()
                }
            }
        }

        listView.setOnItemClickListener { _, _, pos, _ ->
            if (pos !in rows.indices) return@setOnItemClickListener
            val row = rows[pos]
            if (row.isFolder) {
                // 폴더는 단일 선택만 허용: 선택하면 파일 선택 해제
                selectedFilePositions.clear()
                selectedFolderPos = if (selectedFolderPos == pos) null else pos
            } else {
                // 파일은 다중 선택: 파일 선택 시 폴더 선택 해제
                selectedFolderPos = null
                if (selectedFilePositions.contains(pos)) selectedFilePositions.remove(pos)
                else selectedFilePositions.add(pos)
            }
            adapter.notifyDataSetChanged()
            refreshButtons()
        }

        // 길게: 폴더 진입
        listView.setOnItemLongClickListener { _, _, pos, _ ->
            if (pos !in rows.indices) return@setOnItemLongClickListener false
            val row = rows[pos]
            if (row.isFolder) {
                pathStack.add(currentPath)
                currentPath =
                    if (currentPath.endsWith("/")) "${currentPath}${row.name}" else "$currentPath/${row.name}"
                loadPath(currentPath)
                true
            } else false
        }

        btnUp.setOnClickListener {
            if (pathStack.isNotEmpty()) {
                currentPath = pathStack.removeAt(pathStack.lastIndex)
                loadPath(currentPath)
            }
        }

        btnDownload.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                AuthGate.ensureSignedIn()

                btnDownload.isEnabled = false
                btnDownload.text = "다운로드 진행중…"
                try {
                    val (targets, folderName) =
                        if (selectedFolderPos != null) {
                            val folder = rows[selectedFolderPos!!]
                            val refs = listFilesRecursive(folder.ref).distinctBy { it.path }
                            val expanded = withContext(Dispatchers.IO) {
                                refs.map { toDisplayRowFile(it) }
                            }
                            expanded to folder.name
                        } else {
                            val files = selectedFilePositions
                                .filter { it in rows.indices && !rows[it].isFolder }
                                .map { rows[it] }
                            files to null
                        }

                    onDownload(targets, folderName)
                    bind.emptyFileLayout.visibility = View.GONE
                    bind.fullFileLayout.visibility = View.VISIBLE
                    scanDialog?.dismiss()
                } catch (e: Exception) {
                    Toast.makeText(ctx, "실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    bind.emptyFileLayout.visibility = View.VISIBLE
                    bind.fullFileLayout.visibility = View.GONE
                    btnDownload.isEnabled = true
                    btnDownload.text = "다운로드"
                }
            }
        }


        loadPath(currentPath)
        scanDialog?.show()
    }

    private data class DisplayRow(
        val name: String,
        val isFolder: Boolean,
        val timeStr: String = "",
        val sizeStr: String = "",
        val url: String = "",
        val ref: com.google.firebase.storage.StorageReference
    )


    private fun setDot(view: View, color: Int) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(color))
    }

    private suspend fun prefillCollectFromLocal(bind: FragmentMainBinding) {
        withContext(Dispatchers.IO) {
            val base = requireContext().cacheDir
            val mf = findLatestLocal(base, "manifest.txt")
            val a0 = findLatestLocal(base, "app000.fez")
            val a1 = findLatestLocal(base, "app001.fez")
            val a2 = findLatestLocal(base, "app002.fez")
            val present = listOfNotNull(mf, a0, a1, a2)
            val commonFolderRel: String? = if (present.isNotEmpty()) {
                val parents = present.map { parentRelative(base, it) } // ex) "OTA/1"
                commonPrefixPath(parents).ifEmpty { null }
            } else null

            withContext(Dispatchers.Main) {
                bind.selectAllFilePath.text = commonFolderRel

                mf?.let {
                    val sz = it.length()
                    bind.mfstCollectSizeTextview.text = sz.toString()
                    bind.mfstCollectProgress.let {
                        it.visibility = if (sz > 0) View.GONE else View.VISIBLE
                    }
                    bind.mfstCollectPercentTextview.let {
                        it.text = if (sz > 0) "다운로드 완료" else "0%"
                        if (sz > 0) it.setTextColor(Color.parseColor("#29CC6A")) else it.setTextColor(
                            Color.parseColor("#999999")
                        )
                    }
                    collectPercent[1] = if (sz > 0) 100 else 0
                }
                a0?.let {
                    val sz = it.length()
                    bind.app0CollectSizeTextview.text = sz.toString()
                    bind.app0CollectProgress.let {
                        it.visibility = if (sz > 0) View.GONE else View.VISIBLE
                    }
                    bind.app0CollectPercentTextview.let {
                        it.text = if (sz > 0) "다운로드 완료" else "0%"
                        if (sz > 0) it.setTextColor(Color.parseColor("#29CC6A")) else it.setTextColor(
                            Color.parseColor("#999999")
                        )
                    }
                    collectPercent[2] = if (sz > 0) 100 else 0
                }
                a1?.let {
                    val sz = it.length()
                    bind.app1CollectSizeTextview.text = sz.toString()
                    bind.app1CollectProgress.let {
                        it.visibility = if (sz > 0) View.GONE else View.VISIBLE
                    }
                    bind.app1CollectPercentTextview.let {
                        it.text = if (sz > 0) "다운로드 완료" else "0%"
                        if (sz > 0) it.setTextColor(Color.parseColor("#29CC6A")) else it.setTextColor(
                            Color.parseColor("#999999")
                        )
                    }
                    collectPercent[3] = if (sz > 0) 100 else 0
                }
                a2?.let {
                    val sz = it.length()
                    bind.app2CollectSizeTextview.text = sz.toString()
                    bind.app2CollectProgress.let {
                        it.visibility = if (sz > 0) View.GONE else View.VISIBLE
                    }
                    bind.app2CollectPercentTextview.let {
                        it.text = if (sz > 0) "다운로드 완료" else "0%"
                        if (sz > 0) it.setTextColor(Color.parseColor("#29CC6A")) else it.setTextColor(
                            Color.parseColor("#999999")
                        )
                    }
                    collectPercent[4] = if (sz > 0) 100 else 0
                }
                maybeFireAllCollected(bind)
            }
        }
    }

    /** cacheDir 전체(하위 포함)에서 이름이 일치(대소문자 무시)하는 파일 중 최신 수정본 하나 반환 */
    private fun findLatestLocal(root: File, targetName: String): File? {
        var best: File? = null
        root.walkTopDown().forEach { f ->
            if (f.isFile && f.name.equals(targetName, ignoreCase = true)) {
                if (best == null || f.lastModified() > best!!.lastModified()) best = f
            }
        }
        return best
    }

    //    private fun maybeFireAllCollected(bind: FragmentMainBinding) {
//        val all100 = (1..4).all { collectPercent[it] >= 100 }
//        Log.w(TAG, "all100 : ${all100}  |  ${collectPercent.contentToString()}")
//        if (all100 && !allDoneFired) {
//            allDoneFired = true
//            onAllCollected(bind)
//        } else {
//            noAllCollected(bind)
//        }
//    }
    private fun maybeFireAllCollected(bind: FragmentMainBinding) {
        val all100 = (1..4).all { collectPercent[it] >= 100 }
        Log.w(TAG, "all100 : $all100  |  ${collectPercent.contentToString()}")

        if (all100) {
            if (!wasAll100) {
                wasAll100 = true
                allDoneFired = true
                onAllCollected(bind)
            }
        } else {
            wasAll100 = false
            allDoneFired = false
            noAllCollected(bind)
        }
    }

    private fun onAllCollected(bind: FragmentMainBinding) {
        Log.w(TAG, "모든 파일 수집 100%")
        Toast.makeText(requireContext(), "모든 파일 수집 100%", Toast.LENGTH_SHORT).show()
        bind.btnRunAll.isEnabled = true
        bind.selectAllCheckBox.isChecked = true
        val base = requireContext().cacheDir
        val mf = findLatestLocal(base, "manifest.txt")
        val a0 = findLatestLocal(base, "app000.fez")
        val a1 = findLatestLocal(base, "app001.fez")
        val a2 = findLatestLocal(base, "app002.fez")
        val present = listOfNotNull(mf, a0, a1, a2)
        val commonFolderRel: String? = if (present.isNotEmpty()) {
            val parents = present.map { parentRelative(base, it) } // ex) "OTA/1"
            commonPrefixPath(parents).ifEmpty { null }
        } else null
        bind.selectAllFilePath.text = commonFolderRel
        bind.emptyFileLayout.visibility = View.GONE
        bind.fullFileLayout.visibility = View.VISIBLE
    }

    private fun noAllCollected(bind: FragmentMainBinding) {
        Log.w(TAG, "모든 파일 수집 안됨")
        Toast.makeText(requireContext(), "모든 파일 수집 안됨", Toast.LENGTH_SHORT).show()
        bind.btnRunAll.isEnabled = false
        bind.selectAllCheckBox.isChecked = false
//        bind.emptyFileLayout.visibility = View.VISIBLE
//        bind.fullFileLayout.visibility = View.GONE
    }

    private fun parentRelative(base: File, f: File): String {
        val rel = base.toPath().relativize(f.parentFile.toPath()).toString()
        return rel.replace('\\', '/')
    }

    private fun commonPrefixPath(paths: List<String>): String {
        if (paths.isEmpty()) return ""
        val splits = paths.map { it.split('/').filter { seg -> seg.isNotEmpty() } }
        val minLen = splits.minOf { it.size }
        val out = mutableListOf<String>()
        for (i in 0 until minLen) {
            val seg = splits[0][i]
            if (splits.all { it[i] == seg }) out += seg else break
        }
        return out.joinToString("/")
    }


    private fun runOtaWithSlot(slot: Int, bind: FragmentMainBinding) {
        vm.txQueue.clear()
        Toast.makeText(requireContext(), "OTA 시작 (slot=$slot)", Toast.LENGTH_SHORT).show()

        val base = requireContext().cacheDir
        val mf = findLatestLocal(base, "manifest.txt")
        val a0 = findLatestLocal(base, "app000.fez")
        val a1 = findLatestLocal(base, "app001.fez")
        val a2 = findLatestLocal(base, "app002.fez")
        val present = listOfNotNull(mf, a0, a1, a2)

        val commonFolderRel: String? = if (present.isNotEmpty()) {
            val parents = present.map { parentRelative(base, it) } // ex) "OTA/1"
            commonPrefixPath(parents).ifEmpty { null }
        } else null

        val chunk = 20

        val otaFileList = mutableListOf<OtaFileType>().apply {
            if (bind.selectAllCheckBox.isChecked) {
                addAll(
                    listOf(
                        OtaFileType.MANIFEST,
                        OtaFileType.APP000,
                        OtaFileType.APP001,
                        OtaFileType.APP002
                    )
                )
            } else {
                if (bind.mfstCheckBox.isChecked) add(OtaFileType.MANIFEST)
                if (bind.app0CheckBox.isChecked) add(OtaFileType.APP000)
                if (bind.app1CheckBox.isChecked) add(OtaFileType.APP001)
                if (bind.app2CheckBox.isChecked) add(OtaFileType.APP002)
            }
        }

        if (otaFileList.isEmpty()) {
            Toast.makeText(requireContext(), "전송할 파일을 선택하세요.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "전송할 파일을 선택하세요.")
            return
        }
        if (commonFolderRel == null) {
            Toast.makeText(requireContext(), "캐시 폴더를 찾을 수 없습니다. 먼저 파일을 다운로드하세요.", Toast.LENGTH_LONG)
                .show()
            Log.e(TAG, "캐시 폴더를 찾을 수 없습니다. 먼저 파일을 다운로드하세요.")
            return
        }

        Log.w(
            TAG,
            "START_OTA \n commonFolderRel: $commonFolderRel \n slot: $slot \n chunk: $chunk \n otaFileList: $otaFileList"
        )
        val uid = auth.currentUser?.uid
        vm.stageOtaOpsThenEnqueueC3(uid, commonFolderRel, slot, otaFileList)
    }

    private fun humanBytes(n: Long): String {  //2진법이여서 1024바이트 = 1kb 로 해야하나?
        if (n < 1024) return "$n B"
        val kb = n / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }

    //    private fun percentText(pct: Int, eta: String, done: Int, total: Int) =
//        "$pct% \n ETA $eta  ($done/$total)"
//
    private fun percentText(pct: Int, done: Int, total: Int) =
        "$pct% ($done/$total)"


    data class DeviceItem(
        val address: String,
        val name: String,
        val rssi: Int
    )

    private fun showBootBottomSheetAndRun(
        bind: FragmentMainBinding,
        currentBootSlot: Int?,           // 1 또는 2 (infoState에서 파싱해둔 값)
        slot1Date: String,               // "2025.03.27"
        slot2Date: String,               // "2025.01.15"
        isSlot1RecentUpdated: Boolean,   // 최근 업데이트 뱃지 노출 여부
        isSlot2RecentUpdated: Boolean
    ) {
        val dialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_slot_boot, null, false)
        dialog.setContentView(view)

        val card1 = view.findViewById<MaterialCardView>(R.id.cardSlot1)
        val card2 = view.findViewById<MaterialCardView>(R.id.cardSlot2)
        val tvDate1 = view.findViewById<TextView>(R.id.tvDate1)
        val tvDate2 = view.findViewById<TextView>(R.id.tvDate2)
        val badgeCur1 = view.findViewById<TextView>(R.id.badgeCurrent1)
        val badgeCur2 = view.findViewById<TextView>(R.id.badgeCurrent2)
        val badgeRec1 = view.findViewById<TextView>(R.id.badgeRecent1)
        val badgeRec2 = view.findViewById<TextView>(R.id.badgeRecent2)
        val btnConfirm =
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirm)

        // 날짜 세팅
        tvDate1.text = slot1Date
        tvDate2.text = slot2Date

        // 배지 표시
        badgeCur1.visibility = if (currentBootSlot == 1) View.VISIBLE else View.GONE
        badgeCur2.visibility = if (currentBootSlot == 2) View.VISIBLE else View.GONE
        badgeRec1.visibility = if (isSlot1RecentUpdated) View.VISIBLE else View.GONE
        badgeRec2.visibility = if (isSlot2RecentUpdated) View.VISIBLE else View.GONE

        var selected = when (currentBootSlot) {
            1 -> 2
            2 -> 1
            else -> 1 // preBootSlot이 없으면 기본 1
        }

        fun applySelected(card: MaterialCardView, selected: Boolean) {
            val primary = Color.parseColor("#0088FF")
            val surface = Color.WHITE
            val gray = 0xFFE9EAEE.toInt()
            if (selected) {
                card.strokeColor = primary
                card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(
                        primary,
                        (0.12f * 255).toInt()
                    )
                )
            } else {
                card.strokeColor = gray
                card.setCardBackgroundColor(surface)
            }
            // (머티리얼 최신이면 아래 두 줄도 OK. 아니면 없어도 됨)
            card.isChecked = selected
            card.isCheckable = true
        }

        // 초기 UI 반영
        applySelected(card1, selected == 1)
        applySelected(card2, selected == 2)

        // 단일 선택
        fun select(slot: Int) {
            selected = slot
            applySelected(card1, slot == 1)
            applySelected(card2, slot == 2)
        }
        card1.setOnClickListener { select(1) }
        card2.setOnClickListener { select(2) }

        // 확인 버튼
        btnConfirm.setOnClickListener {
//            bind.versionTxt.text = selected.toString()
            dialog.dismiss()
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = vm.sendSelectCommand(selected)
                runCatching {
                    vm.selectOtaSession(
                        status = "selectRunning",
                        slotSelected = selected,
                        sessionType = "SELECT_RUNNING"
                    )
                }.onFailure { e -> Log.e(TAG, "selectOtaSession failed", e) }
                Log.d(TAG, "send HEADER_INFO_SELECT_COMMAND result=$ok")
            }
        }

        dialog.show()
    }


    private fun showSlotBottomSheetAndRun(
        bind: FragmentMainBinding,
        currentBootSlot: Int?,           // 1 또는 2 (infoState에서 파싱해둔 값)
        slot1Date: String,               // "2025.03.27"
        slot2Date: String,               // "2025.01.15"
        isSlot1RecentUpdated: Boolean,   // 최근 업데이트 뱃지 노출 여부
        isSlot2RecentUpdated: Boolean
    ) {
        val dialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_slot_select, null, false)
        dialog.setContentView(view)

        val card1 = view.findViewById<MaterialCardView>(R.id.cardSlot1)
        val card2 = view.findViewById<MaterialCardView>(R.id.cardSlot2)
        val tvDate1 = view.findViewById<TextView>(R.id.tvDate1)
        val tvDate2 = view.findViewById<TextView>(R.id.tvDate2)
        val badgeCur1 = view.findViewById<TextView>(R.id.badgeCurrent1)
        val badgeCur2 = view.findViewById<TextView>(R.id.badgeCurrent2)
        val badgeRec1 = view.findViewById<TextView>(R.id.badgeRecent1)
        val badgeRec2 = view.findViewById<TextView>(R.id.badgeRecent2)
        val btnConfirm =
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirm)

        // 날짜 세팅
        tvDate1.text = slot1Date
        tvDate2.text = slot2Date

        // 배지 표시
        badgeCur1.visibility = if (currentBootSlot == 1) View.VISIBLE else View.GONE
        badgeCur2.visibility = if (currentBootSlot == 2) View.VISIBLE else View.GONE
        badgeRec1.visibility = if (isSlot1RecentUpdated) View.VISIBLE else View.GONE
        badgeRec2.visibility = if (isSlot2RecentUpdated) View.VISIBLE else View.GONE

        var selected = when (currentBootSlot) {
            1 -> 2
            2 -> 1
            else -> 1 // preBootSlot이 없으면 기본 1
        }

        fun applySelected(card: MaterialCardView, selected: Boolean) {
            val primary = Color.parseColor("#0088FF")
            val surface = Color.WHITE
            val gray = 0xFFE9EAEE.toInt()
            if (selected) {
                card.strokeColor = primary
                card.setCardBackgroundColor(
                    ColorUtils.setAlphaComponent(
                        primary,
                        (0.12f * 255).toInt()
                    )
                )
            } else {
                card.strokeColor = gray
                card.setCardBackgroundColor(surface)
            }
            // (머티리얼 최신이면 아래 두 줄도 OK. 아니면 없어도 됨)
            card.isChecked = selected
            card.isCheckable = true
        }

        // 초기 UI 반영
        applySelected(card1, selected == 1)
        applySelected(card2, selected == 2)

        // 단일 선택
        fun select(slot: Int) {
            selected = slot
            applySelected(card1, slot == 1)
            applySelected(card2, slot == 2)
        }
        card1.setOnClickListener { select(1) }
        card2.setOnClickListener { select(2) }

        // 확인 버튼
        btnConfirm.setOnClickListener {
            // 탭 UI도 동기화
            bind.versionTxt.text = selected.toString()
            dialog.dismiss()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(250L)
                runOtaWithSlot(selected, bind)
            }
        }

        dialog.show()
    }

    class DeviceAdapter(
        private val onClick: (DeviceItem) -> Unit
    ) : ListAdapter<DeviceItem, DeviceAdapter.VH>(diff) {

        companion object {
            private val diff = object : DiffUtil.ItemCallback<DeviceItem>() {
                override fun areItemsTheSame(a: DeviceItem, b: DeviceItem) = a.address == b.address
                override fun areContentsTheSame(a: DeviceItem, b: DeviceItem) = a == b
            }
        }

        inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
            private val title = v.findViewById<TextView>(android.R.id.text1)
            fun bind(item: DeviceItem) {
                title.text = "${item.name.ifBlank { "알 수 없음" }} (${item.rssi} dBm)"
                v.setOnClickListener { onClick(item) }
            }
        }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
            val view = LayoutInflater.from(p.context)
                .inflate(android.R.layout.simple_list_item_1, p, false)
            return VH(view)
        }

        override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    }

    fun showTooltip(anchor: View, text: String) {
        val inflater = LayoutInflater.from(anchor.context)
        val popupView = inflater.inflate(R.layout.tooltip_layout, null)
        val tvTooltip = popupView.findViewById<TextView>(R.id.tvTooltip)
        tvTooltip.text = text

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        // anchor 기준 위치 조정 (살짝 위쪽에 표시)
        popupWindow.showAsDropDown(anchor, 0, -anchor.height - 40)

        // 2초 뒤 자동 닫기
        Handler(Looper.getMainLooper()).postDelayed({
            if (popupWindow.isShowing) popupWindow.dismiss()
        }, 2000)
    }


}
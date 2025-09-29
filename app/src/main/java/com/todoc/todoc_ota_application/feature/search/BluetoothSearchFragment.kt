package com.todoc.todoc_ota_application.feature.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.todoc.todoc_ota_application.R
import com.todoc.todoc_ota_application.core.model.DetailedConnectionState
import com.todoc.todoc_ota_application.data.ble.ConnectionState
import com.todoc.todoc_ota_application.data.ble.ScanningState
import com.todoc.todoc_ota_application.feature.login.LoginViewModel
import com.todoc.todoc_ota_application.feature.login.LoginViewModelFactory
import com.todoc.todoc_ota_application.feature.login.data.FirebaseAuthRepository
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import com.todoc.todoc_ota_application.feature.main.MainFragment
import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BluetoothSearchFragment : Fragment(R.layout.fragment_bluetooth_serarch) {

    private val TAG = this.javaClass.simpleName
    private val vm: MainViewModel by activityViewModels()
    private val viewModel: LoginViewModel by lazy {
        val repo = FirebaseAuthRepository(requireContext())
        ViewModelProvider(this, LoginViewModelFactory(repo))[LoginViewModel::class.java]
    }

    // 기존 뷰들
    private lateinit var tvStatus: TextView
    private lateinit var pbScan: ProgressBar
    private lateinit var btnRefresh: TextView
    private lateinit var backToLoginBtn: ImageView
    private lateinit var rv: RecyclerView
    private lateinit var tilKey: TextInputLayout
    private lateinit var etKey: TextInputEditText
    private lateinit var btnConnect: Button

    // 전체화면 로딩 UI
    private lateinit var loadingOverlay: View
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loadingStatusText: TextView
    private lateinit var loadingDeviceName: TextView
    private lateinit var loadingCancelBtn: Button

    private val adapter = DeviceListAdapter { item ->
        if (!isConnecting) {
            selectedItem = item
            updateConnectEnabled()
        }
    }

    private var selectedItem: MainFragment.DeviceItem? = null
    private var hasNavigated = false
    private var isConnecting = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        bindRecycler()
        bindButtons()
        collectFlows()
    }
    private fun bindRecycler() {
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
    }
    private fun bindViews(v: View) {
        // 기존 뷰들
        tvStatus   = v.findViewById(R.id.tvStatus)
        pbScan     = v.findViewById(R.id.pbScan)
        btnRefresh = v.findViewById(R.id.btnRefresh)
        backToLoginBtn = v.findViewById(R.id.backToLoginBtn)
        rv         = v.findViewById(R.id.rvDevices)
        tilKey      = v.findViewById(R.id.tilKey)
        etKey       = v.findViewById(R.id.etKey)
        btnConnect  = v.findViewById(R.id.btnConnect)

        // 전체화면 로딩 오버레이
        loadingOverlay = v.findViewById(R.id.loadingOverlay)
        loadingProgressBar = v.findViewById(R.id.loadingProgressBar)
        loadingStatusText = v.findViewById(R.id.loadingStatusText)
        loadingDeviceName = v.findViewById(R.id.loadingDeviceName)
        loadingCancelBtn = v.findViewById(R.id.loadingCancelBtn)

        // 취소 버튼 클릭
        loadingCancelBtn.setOnClickListener {
            cancelConnection()
        }
    }

    private fun bindButtons() {
        val nav = findNavController()
        backToLoginBtn.setOnClickListener {
            if (isConnecting) {
                Toast.makeText(requireContext(), "연결 진행 중입니다. 취소 후 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                vm.justDisconnect()
                val repo = LocalAuthRepository(requireContext())
                repo.clearAll()
                viewModel.logout()
                nav.navigate(R.id.action_search_to_login)
            }
        }

        btnRefresh.setOnClickListener {
            if (!isConnecting) {
                startScan()
            }
        }

        btnConnect.setOnClickListener {
            if (isConnecting) return@setOnClickListener

            val item = selectedItem ?: return@setOnClickListener
            val key  = etKey.text?.toString()?.trim().orEmpty()

            if (key.isEmpty()) {
                tilKey.error = "보안키를 입력하세요"
                return@setOnClickListener
            }
            tilKey.error = null

            startConnection(item, key)
        }

        etKey.addTextChangedListener {
            updateConnectEnabled()
        }
    }

    private fun startConnection(item: MainFragment.DeviceItem, key: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // 전체화면 로딩 시작
            showLoadingOverlay(item.name, "연결 준비 중...")

            LocalAuthRepository(requireContext()).savePasskey(key)

            val target = vm.devices.value.firstOrNull { it.device.address == item.address }
            if (target == null) {
                hideLoadingOverlay()
                Toast.makeText(requireContext(), "장치가 목록에 없습니다. 다시 스캔하세요.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            vm.connectTo(target)
        }
    }
    private fun updateLoadingStatus(status: String, step: Int = 1) {
        if (isConnecting) {
            loadingStatusText.text = status
            // 진행률 표시 업데이트
            updateProgressSteps(step)
        }
    }

    private fun updateProgressSteps(currentStep: Int) {
        val progressViews = listOf(
            view?.findViewById<View>(R.id.progress1),
            view?.findViewById<View>(R.id.progress2),
            view?.findViewById<View>(R.id.progress3),
            view?.findViewById<View>(R.id.progress4)
        )

        progressViews.forEachIndexed { index, progressView ->
            progressView?.setBackgroundResource(
                if (index < currentStep) R.drawable.circle_progress_active
                else R.drawable.circle_progress_inactive
            )
        }
    }
    private fun showLoadingOverlay(deviceName: String, status: String) {
        isConnecting = true

        loadingDeviceName.text = deviceName
        updateLoadingStatus(status, 1)
        loadingOverlay.isVisible = true

        // 기존 UI 비활성화 (필요시)
        btnConnect.isEnabled = false
        btnRefresh.isEnabled = false
        rv.isClickable = false
        etKey.isEnabled = false
    }

    private fun hideLoadingOverlay() {
        isConnecting = false
        loadingOverlay.isVisible = false

        // 기존 UI 활성화
        updateConnectEnabled()
        btnRefresh.isEnabled = true
        rv.isClickable = true
        etKey.isEnabled = true
    }



    private fun cancelConnection() {
        lifecycleScope.launch {
            vm.justDisconnect()
            hideLoadingOverlay()
            Toast.makeText(requireContext(), "연결이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateConnectEnabled() {
        if (isConnecting) {
            btnConnect.isEnabled = false
            return
        }

        val hasSelection = selectedItem != null
        val keyOk = !etKey.text.isNullOrBlank()
        btnConnect.isEnabled = hasSelection && keyOk
    }

    @SuppressLint("MissingPermission")
    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.scanning.collectLatest { st ->
                        when (st) {
                            is ScanningState.Scanning -> {
                                tvStatus.text = "검색된 기기 : 스캔 중…"
                                pbScan.isVisible = true
                            }
                            is ScanningState.NotScanning -> {
                                tvStatus.text = "검색된 기기 : 스캔 준비"
                                pbScan.isVisible = false
                            }
                        }
                    }
                }

                launch {
                    vm.devices
                        .map { results ->
                            results
                                .distinctBy { it.device.address }
                                .filter { it.rssi > -90 }
                                .sortedBy { it.device.name ?: it.device.address ?: "" }
                                .take(4)
                                .map {
                                    MainFragment.DeviceItem(
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

                launch {
                    vm.connection.collectLatest { st ->
                        Log.d(TAG, "connection state=$st")
                        when (st) {
                            is ConnectionState.Connected -> {
                                updateLoadingStatus("연결 완료!", 4)
                                // 0.5초 후 화면 전환 (성공 메시지 표시 시간)
                                delay(500)
                                hideLoadingOverlay()

                                if (findNavController().currentDestination?.id == R.id.bluetoothSearchFragment) {
                                    findNavController().navigate(R.id.action_search_to_versionCheck)
                                }
                            }

                            ConnectionState.Disconnected -> {
                                if (isConnecting) {
                                    hideLoadingOverlay()
                                    Toast.makeText(requireContext(), "연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            ConnectionState.Connecting -> {
                                updateLoadingStatus("BLE 연결 중...", 2)
                            }
                        }
                    }
                }

                // BleConnector의 세부 상태 모니터링
                launch {
                    vm.detailedConnection.collectLatest { detailState ->
                        if (isConnecting) {
                            val (statusText, step) = when (detailState) {
                                DetailedConnectionState.Connecting -> "BLE 연결 중..." to 1
                                DetailedConnectionState.Connected -> "기기 연결됨" to 2
                                DetailedConnectionState.ServiceDiscovering -> "서비스 검색 중..." to 2
                                DetailedConnectionState.ServiceDiscovered -> "서비스 발견 완료" to 3
                                DetailedConnectionState.DescriptorSetting -> "알림 설정 중..." to 3
                                DetailedConnectionState.NotificationEnabled -> "인증 준비 중..." to 3
                                DetailedConnectionState.FullyReady -> "인증 진행 중..." to 4
                                else -> "연결 중..." to 1
                            }
                            updateLoadingStatus(statusText, step)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isConnecting) {
            startScan()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isConnecting) {
            vm.stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (isConnecting) return

        selectedItem = null
        updateConnectEnabled()
        vm.startScan()
    }

    private class DeviceListAdapter(
        val onClick: (MainFragment.DeviceItem) -> Unit
    ) : RecyclerView.Adapter<DeviceListAdapter.VH>() {

        private var selectedAddress: String? = null

        // DiffUtil + AsyncListDiffer
        private val diff = object : DiffUtil.ItemCallback<MainFragment.DeviceItem>() {
            override fun areItemsTheSame(a: MainFragment.DeviceItem, b: MainFragment.DeviceItem) =
                a.address == b.address
            override fun areContentsTheSame(a: MainFragment.DeviceItem, b: MainFragment.DeviceItem) =
                a == b
        }
        private val differ = AsyncListDiffer(this, diff)

        /** 외부에서 리스트 갱신할 때 이걸 호출 */
        fun submitList(newItems: List<MainFragment.DeviceItem>) {
            differ.submitList(newItems)
        }

        /** 선택 하이라이트 갱신 */
        private fun select(address: String) {
            selectedAddress = address
            notifyDataSetChanged() // 선택만 바뀌면 이 정도로 충분
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(v, onClick) { addr -> select(addr) }
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = differ.currentList[position]
            holder.bind(item, item.address == selectedAddress)
        }

        override fun getItemCount() = differ.currentList.size

        class VH(
            private val v: View,
            private val onClick: (MainFragment.DeviceItem) -> Unit,
            private val onSelect: (String) -> Unit
        ) : RecyclerView.ViewHolder(v) {
            private val t1 = v.findViewById<TextView>(android.R.id.text1)
            private val t2 = v.findViewById<TextView>(android.R.id.text2)

            fun bind(item: MainFragment.DeviceItem, selected: Boolean) {
                // 터치 영역 확보(선택)
                v.minimumHeight = v.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)

                t1.text = item.name.ifBlank { "알 수 없음" }
                t2.text = "${item.address} • ${item.rssi} dBm"

                val ctx = v.context
                if (selected) {
                    v.setBackgroundColor(ctx.getColor(R.color.select_box))
                    t1.setTextColor(ctx.getColor(R.color.black))
                    t2.setTextColor(ctx.getColor(R.color.gray))
                } else {
                    v.setBackgroundColor(ctx.getColor(android.R.color.transparent))
                    t1.setTextColor(ctx.getColor(R.color.black))
                    t2.setTextColor(ctx.getColor(R.color.gray))
                }

                v.setOnClickListener {
                    onClick(item)
                    onSelect(item.address)
                }
            }
        }
    }



}

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
import com.todoc.todoc_ota_application.data.ble.ConnectionState
import com.todoc.todoc_ota_application.data.ble.ScanningState
import com.todoc.todoc_ota_application.feature.login.LoginViewModel
import com.todoc.todoc_ota_application.feature.login.LoginViewModelFactory
import com.todoc.todoc_ota_application.feature.login.data.FirebaseAuthRepository
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import com.todoc.todoc_ota_application.feature.main.MainFragment
import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BluetoothSearchFragment : Fragment(R.layout.fragment_bluetooth_serarch) {

    private val TAG = this.javaClass.simpleName
    private val vm: MainViewModel by activityViewModels()
    private val viewModel: LoginViewModel by lazy {
        val repo = FirebaseAuthRepository(requireContext())
        ViewModelProvider(this, LoginViewModelFactory(repo))[LoginViewModel::class.java]
    }
    private lateinit var tvStatus: TextView
    private lateinit var pbScan: ProgressBar
    private lateinit var btnRefresh: TextView
    private lateinit var backToLoginBtn: ImageView
    private lateinit var rv: RecyclerView

    private lateinit var tilKey: TextInputLayout
    private lateinit var etKey: TextInputEditText
    private lateinit var btnConnect: Button

    private val adapter = DeviceListAdapter { item ->
        selectedItem = item
        updateConnectEnabled()
    }

    private var selectedItem: MainFragment.DeviceItem? = null
    private var hasNavigated = false   // 중복 popBackStack 방지
    private var isConnecting = false   // 버튼 중복 클릭 방지

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        bindRecycler()
        bindButtons()
        collectFlows()
    }

    private fun bindViews(v: View) {
        tvStatus   = v.findViewById(R.id.tvStatus)
        pbScan     = v.findViewById(R.id.pbScan)
        btnRefresh = v.findViewById(R.id.btnRefresh)
        backToLoginBtn = v.findViewById(R.id.backToLoginBtn)
        rv         = v.findViewById(R.id.rvDevices)

        tilKey      = v.findViewById(R.id.tilKey)
        etKey       = v.findViewById(R.id.etKey)
        btnConnect  = v.findViewById(R.id.btnConnect)

    }

    private fun bindRecycler() {
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
    }

    private fun bindButtons() {
        val nav = findNavController()
        backToLoginBtn.setOnClickListener {
            lifecycleScope.launch {
                vm.justDisconnect()
                val repo = LocalAuthRepository(requireContext())
                repo.clearAll()
                viewModel.logout()
                nav.navigate(R.id.action_search_to_login)
            }

        }
        btnRefresh.setOnClickListener {
            startScan()
        }
        btnConnect.setOnClickListener {

            val item = selectedItem ?: return@setOnClickListener
            val key  = etKey.text?.toString()?.trim().orEmpty()

            if (key.isEmpty()) {
                tilKey.error = "보안키를 입력하세요"
                return@setOnClickListener
            }
            tilKey.error = null

            // address로 ScanResult 재조회 후 connect
            viewLifecycleOwner.lifecycleScope.launch {

                LocalAuthRepository(requireContext()).savePasskey(key)
                val target = vm.devices.value.firstOrNull { it.device.address == item.address }
                if (target == null) {
                    Toast.makeText(requireContext(), "장치가 목록에 없습니다. 다시 스캔하세요.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                Toast.makeText(requireContext(), "연결 시도 중…", Toast.LENGTH_SHORT).show()

                vm.connectTo(target)
//                findNavController().popBackStack() // 성공/실패는 메인 화면에서 상태로 노출
            }
        }

        // 입력 바뀔 때 버튼 활성/비활성
        etKey.addTextChangedListener {
            updateConnectEnabled()
        }
    }

    private fun updateConnectEnabled() {
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
                                .sortedBy { it.device.name ?: it.device.address ?: "" } // 이름순 정렬
                                .take(4) // 최대 4개만 표시
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
                                isConnecting = false
                                updateConnectEnabled()
//                                if (!hasNavigated && findNavController().previousBackStackEntry != null) {
//                                    hasNavigated = true
//                                    if (findNavController().currentDestination?.id == R.id.bluetoothSearchFragment) {
//                                        findNavController().navigate(R.id.action_search_to_main)
//                                    }
//                                }
                                if (findNavController().currentDestination?.id == R.id.bluetoothSearchFragment) {
                                    findNavController().navigate(R.id.action_search_to_versionCheck)
                                }

                            }
                            ConnectionState.Disconnected -> {
                                isConnecting = false
                                updateConnectEnabled()
                                // 필요 시 실패 토스트 등
                            }
                            ConnectionState.Connecting -> {
                                // UI에 표시만
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startScan()
    }

    override fun onPause() {
        super.onPause()
        vm.stopScan()
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
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

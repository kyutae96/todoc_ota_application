package com.todoc.todoc_ota_application.feature.versionCheck

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.BLUE
import android.graphics.Color.RED
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
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
import com.todoc.todoc_ota_application.databinding.FragmentMainBinding
import com.todoc.todoc_ota_application.databinding.FragmentVersionCheckBinding
import com.todoc.todoc_ota_application.feature.login.LoginViewModel
import com.todoc.todoc_ota_application.feature.login.LoginViewModelFactory
import com.todoc.todoc_ota_application.feature.login.data.FirebaseAuthRepository
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import com.todoc.todoc_ota_application.feature.main.MainFragment
import com.todoc.todoc_ota_application.feature.main.MainFragment.DeviceAdapter
import com.todoc.todoc_ota_application.feature.main.MainFragment.DeviceItem
import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VersionCheckFragment : Fragment(R.layout.fragment_version_check) {

    private val TAG = this.javaClass.simpleName
    private val vm: MainViewModel by activityViewModels()
    private val viewModel: LoginViewModel by lazy {
        val repo = FirebaseAuthRepository(requireContext())
        ViewModelProvider(this, LoginViewModelFactory(repo))[LoginViewModel::class.java]
    }

    private var scanDialog: AlertDialog? = null
    private var scanCollectJob: Job? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bind = FragmentVersionCheckBinding.bind(view)
        bindButtons(bind)
        collectFlows(bind)
    }



    @SuppressLint("MissingPermission")
    private fun bindButtons(bind: FragmentVersionCheckBinding) {
        val nav = findNavController()
        bind.backToSearchBtn.setOnClickListener {
            lifecycleScope.launch {
                vm.justDisconnect()
                val repo = LocalAuthRepository(requireContext())
                repo.clearAll()
//                viewModel.logout()
                nav.navigate(R.id.action_version_to_search)
            }
        }
        bind.scanBtn.setOnClickListener {
            Log.w(TAG, "START_SCAN")
            Toast.makeText(requireContext(), "스캔 시작", Toast.LENGTH_SHORT).show()

            if (scanDialog?.isShowing != true) showScanDialog()
        }

        bind.manualUpdateBtn.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.versionCheckFragment) {
                findNavController().navigate(R.id.action_version_to_main)
            }
        }

        bind.autoUpdateBtn.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.versionCheckFragment) {
                findNavController().navigate(R.id.action_version_to_auto)
            }
        }


    }



    @SuppressLint("MissingPermission")
    private fun collectFlows(bind: FragmentVersionCheckBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    val latestFolder = vm.getLatestOtaFolder()
                    bind.latestVersionTxt.text = latestFolder
                    Log.d(TAG, "가장 최근 OTA 폴더: $latestFolder")
                }
                launch {
                    vm.scanning.collectLatest { st ->
                        when (st) {
                            is ScanningState.NotScanning -> {
                                Log.w(TAG, "NotScanning")
                                bind.tvDeviceName.text = "연결 없음 / 스캔 시작"
                                bind.currentVersionTxt.text = "---"
//                        bind.btnRunAll.visibility = View.GONE
                            }

                            is ScanningState.Scanning -> {
                                Log.w(TAG, "Scanning")
                                bind.tvDeviceName.text = "스캔 진행중..."
                                bind.currentVersionTxt.text = "---"
//                        bind.btnRunAll.visibility = View.GONE
                            }
                        }
                    }
                }
                launch {
                    vm.connection.collectLatest { st ->
                        when (st) {
                            is ConnectionState.Connected -> {
                                Log.w(TAG, "Connected")
                                val deviceName = st.device.name ?: st.device.address ?: "알 수 없음"
                                bind.tvDeviceName.text = deviceName
                                val latestPath = vm.getLatestSourcePath(deviceName)
                                bind.currentVersionTxt.text = latestPath
                                Log.d(TAG, "최근 sourcePath: $latestPath")
//                        bind.btnRunAll.isEnabled = true
                                setDot(bind.viewStatusDot, BLUE)
                                LocalAuthRepository(requireContext()).savePairingDevice(st.device.name)
                                dismissScanDialog()

                                val isMatch = vm.isLatestSourcePathMatching(deviceName)
                                if (isMatch) {
                                    bind.updateLinearLayout.setBackgroundResource(R.drawable.gray_rectangle)
                                    bind.matchVersionTxt.text = "현재 기기가 최신 상태 입니다."
                                } else {
                                    bind.updateLinearLayout.setBackgroundResource(R.drawable.blue_stroke_rectangle)
                                    bind.matchVersionTxt.text = "현재 기기가 최신 상태가 아닙니다.\n업데이트를 진행하여 기기를 최신 상태로 유지하세요."
                                    vm.setTxtFromLatestOtaFolder(
                                        fileName = "readme.txt",
                                        textView = bind.readmeTxt,
                                        progressBar = bind.progressBar
                                    )
                                }
                            }

                            ConnectionState.Connecting -> {
                                Log.w(TAG, "Connecting")
                                bind.tvDeviceName.text = "페어링 진행중..."
                                bind.currentVersionTxt.text = "---"
//                        bind.btnRunAll.visibility = View.GONE
                                setDot(bind.viewStatusDot, Color.parseColor("#FFA000"))
                            }

                            ConnectionState.Disconnected -> {
                                Log.w(TAG, "Disconnected")
//                        bind.btnRunAll.visibility = View.GONE
                                bind.tvDeviceName.text = "연결 없음 / 스캔 시작"
                                bind.currentVersionTxt.text = "---"
                                setDot(bind.viewStatusDot, RED)
                            }
                        }
                    }
                }
                launch {
                    vm.internalSerial.collectLatest { serial ->
                        bind.tvDeviceSerial.text = "#$serial"
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
//        startScan()
    }

    override fun onPause() {
        super.onPause()
//        vm.stopScan()
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




    private fun setDot(view: View, color: Int) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(color))
    }
    private fun dismissScanDialog() {
        scanCollectJob?.cancel()
        scanDialog?.dismiss()
        scanDialog = null
    }

}

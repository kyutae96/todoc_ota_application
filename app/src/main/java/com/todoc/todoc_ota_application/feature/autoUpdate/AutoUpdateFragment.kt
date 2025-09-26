package com.todoc.todoc_ota_application.feature.autoUpdate

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.todoc.todoc_ota_application.databinding.FragmentAutoUpdateBinding
import com.todoc.todoc_ota_application.databinding.FragmentBluetoothSerarchBinding
import com.todoc.todoc_ota_application.databinding.FragmentVersionCheckBinding
import com.todoc.todoc_ota_application.feature.login.LoginViewModel
import com.todoc.todoc_ota_application.feature.login.LoginViewModelFactory
import com.todoc.todoc_ota_application.feature.login.data.FirebaseAuthRepository
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import com.todoc.todoc_ota_application.feature.main.MainFragment
import com.todoc.todoc_ota_application.feature.main.MainViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AutoUpdateFragment : Fragment(R.layout.fragment_auto_update) {

    private val TAG = this.javaClass.simpleName
    private val vm: MainViewModel by activityViewModels()
    private val viewModel: LoginViewModel by lazy {
        val repo = FirebaseAuthRepository(requireContext())
        ViewModelProvider(this, LoginViewModelFactory(repo))[LoginViewModel::class.java]
    }

    private var selectedItem: MainFragment.DeviceItem? = null
    private var hasNavigated = false   // 중복 popBackStack 방지
    private var isConnecting = false   // 버튼 중복 클릭 방지

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bind = FragmentAutoUpdateBinding.bind(view)
        bindButtons(bind)
        collectFlows(bind)
    }



    private fun bindButtons(bind: FragmentAutoUpdateBinding) {
        val nav = findNavController()
        bind.backToSearchBtn.setOnClickListener {
//            lifecycleScope.launch {
//                vm.justDisconnect()
//                val repo = LocalAuthRepository(requireContext())
//                repo.clearAll()
//                viewModel.logout()
//                nav.navigate(R.id.action_search_to_login)
//            }
        }

    }


    @SuppressLint("MissingPermission")
    private fun collectFlows(bind: FragmentAutoUpdateBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {

                    }
                }
            }
    }



}

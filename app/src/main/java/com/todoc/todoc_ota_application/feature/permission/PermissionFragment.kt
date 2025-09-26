package com.todoc.todoc_ota_application.feature.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.todoc.todoc_ota_application.R

class PermissionFragment : Fragment(R.layout.fragment_permission) {

    private val permissions = PermissionHelper.requiredBlePermissions()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.all { it.value }
            if (allGranted) {
                goNext()
            } else {
                Toast.makeText(requireContext(), "필수 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            AlertDialog.Builder(requireContext())
                .setTitle("지원 불가 버전")
                .setMessage("이 앱은 Android 9(P) 이상에서만 동작합니다.")
                .setPositiveButton("닫기") { _, _ -> requireActivity().finish() }
                .setCancelable(false)
                .show()
            return
        }

        if (!PermissionHelper.hasAllPermissions(requireContext(), permissions)) {
            requestPermissionLauncher.launch(permissions)
        }

        // 버튼: 설정 안내 or 다음 화면
        view.findViewById<TextView>(R.id.permissionCheckedBtn).setOnClickListener {
            if (!PermissionHelper.hasAllPermissions(requireContext(), permissions)) {
                Toast.makeText(requireContext(), "설정에서 권한을 허용해 주세요.", Toast.LENGTH_SHORT).show()
                openAppSettings()
            } else {
                goNext()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    private fun goNext() {
        findNavController().navigate(R.id.action_permission_to_login)
    }
}

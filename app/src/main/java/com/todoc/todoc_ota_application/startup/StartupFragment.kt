package com.todoc.todoc_ota_application.startup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.todoc.todoc_ota_application.R
import com.todoc.todoc_ota_application.feature.permission.PermissionHelper
import com.todoc.todoc_ota_application.feature.login.data.LocalAuthRepository
import kotlinx.coroutines.launch

class StartupFragment : Fragment(R.layout.empty_view) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nav = findNavController()
        val hasPerm = PermissionHelper.hasAllPermissions(requireContext())
        if (!hasPerm) {
            nav.navigate(R.id.action_startup_to_permission)
            return
        }

        lifecycleScope.launch {
//            val repo = LocalAuthRepository(requireContext())
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (firebaseUser != null
//                && repo.isLoggedIn()
                ) {
                // 퍼미션 O + Firebase 인증 O → Search/메인 화면으로
                nav.navigate(R.id.action_startup_to_search)
            } else {
                // 로그인 안 되어 있으면 로그인 화면으로
                nav.navigate(R.id.action_startup_to_login)
            }
        }
    }
}

package com.todoc.todoc_ota_application.feature.login

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ViewSwitcher
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.todoc.todoc_ota_application.R
import com.todoc.todoc_ota_application.feature.login.data.FirebaseAuthRepository
import com.todoc.todoc_ota_application.feature.login.data.Role
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class LoginViewModelFactory(
    private val repo: FirebaseAuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(c: Class<T>): T {
        return LoginViewModel(repo) as T
    }
}

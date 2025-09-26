package com.todoc.todoc_ota_application.feature.login

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
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

class LoginFragment : Fragment(R.layout.fragment_login) {

    // ViewModel (간단 팩토리로 repo 주입)
    private val viewModel: LoginViewModel by lazy {
        val repo = FirebaseAuthRepository(requireContext())
        ViewModelProvider(this, LoginViewModelFactory(repo))[LoginViewModel::class.java]
    }

    // Views
    private lateinit var tabLayout: TabLayout
    private lateinit var viewSwitcher: ViewSwitcher

    // Login views
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var pbLogin: ProgressBar
    private lateinit var loginForm: LinearLayout

    // Signup views
    private lateinit var etSignupName: EditText
    private lateinit var etSignupOrg: EditText
    private lateinit var etSignupEmail: EditText
    private lateinit var etSignupPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var pbSignup: ProgressBar
    private lateinit var signupForm: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        bindTabs()
        bindActions()
        observeState()
    }

    private fun bindViews(root: View) {
        tabLayout = root.findViewById(R.id.tabLayout)
        viewSwitcher = root.findViewById(R.id.viewSwitcher)

        // login
        etLoginEmail = root.findViewById(R.id.etLoginEmail)
        etLoginPassword = root.findViewById(R.id.etLoginPassword)
        btnLogin = root.findViewById(R.id.btnLogin)
        pbLogin = root.findViewById(R.id.pbLogin)
        loginForm = root.findViewById(R.id.loginForm)

        // signup
        etSignupName = root.findViewById(R.id.etSignupName)
        etSignupOrg = root.findViewById(R.id.etSignupOrg)
        etSignupEmail = root.findViewById(R.id.etSignupEmail)
        etSignupPassword = root.findViewById(R.id.etSignupPassword)
        btnSignup = root.findViewById(R.id.btnSignup)
        pbSignup = root.findViewById(R.id.pbSignup)
        signupForm = root.findViewById(R.id.signupForm)
    }

    private fun bindTabs() {
        // Tab 0 = Log In, Tab 1 = Sign Up
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val showingLogin = viewSwitcher.currentView.id == R.id.loginForm
                when (tab.position) {
                    0 -> if (!showingLogin) viewSwitcher.showPrevious()
                    1 -> if (showingLogin) viewSwitcher.showNext()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun bindActions() {
        // IME done → 로그인
        etLoginPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnLogin.performClick()
                true
            } else false
        }

        btnLogin.setOnClickListener {
            val email = etLoginEmail.text?.toString()?.trim().orEmpty()
            val pw = etLoginPassword.text?.toString().orEmpty()

            if (!email.isValidEmail()) {
                etLoginEmail.error = getString(R.string.error_invalid_email)
                return@setOnClickListener
            }
            if (pw.length < 6) {
                etLoginPassword.error = getString(R.string.error_short_password)
                return@setOnClickListener
            }

            setLoginLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.login(email, pw)
            }
        }

        btnSignup.setOnClickListener {
            val name = etSignupName.text?.toString()?.trim().orEmpty()
            val org = etSignupOrg.text?.toString()?.trim().orEmpty()
            val email = etSignupEmail.text?.toString()?.trim().orEmpty()
            val pw = etSignupPassword.text?.toString().orEmpty()

            if (name.isEmpty()) {
                etSignupName.error = getString(R.string.error_required)
                return@setOnClickListener
            }
            if (org.isEmpty()) {
                etSignupOrg.error = getString(R.string.error_required)
                return@setOnClickListener
            }
            if (!email.isValidEmail()) {
                etSignupEmail.error = getString(R.string.error_invalid_email)
                return@setOnClickListener
            }
            if (pw.length < 6) {
                etSignupPassword.error = getString(R.string.error_short_password)
                return@setOnClickListener
            }

            setSignupLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.signup(email, pw, name, org)
            }
        }
    }

    private fun observeState() {
        // 로딩
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .distinctUntilChanged { old, new -> old.loading == new.loading }
                .collect { st ->
                    if (viewSwitcher.currentView.id == R.id.loginForm) {
                        setLoginLoading(st.loading)
                    } else {
                        setSignupLoading(st.loading)
                    }
                }
        }

        // 에러
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .distinctUntilChanged { old, new -> old.error == new.error }
                .collect { st ->
                    st.error?.let { showSnack(requireView(), it) }
                }
        }

        // 유저/권한 변화 → 내비게이션 or 승인대기 안내
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { st ->
                val role = st.role
                val user = st.user

                if (user == null) return@collect

                when (role) {
                    Role.ADMIN, Role.MANAGER -> {
                        // 대시보드로 이동 (네비게이션 그래프의 액션 ID는 프로젝트에 맞춰 변경)
                        if (findNavController().currentDestination?.id == R.id.loginFragment) {
                            findNavController().navigate(R.id.action_login_to_search)
                        }
                    }
                    Role.UNAUTHORIZED -> {
                        // 승인대기 안내
                        showSnack(
                            requireView(),
                            getString(R.string.msg_awaiting_approval)
                        )
                        // 로그인 탭으로 돌려놓음(선택)
                        if (viewSwitcher.currentView.id == R.id.signupForm) {
                            tabLayout.getTabAt(0)?.select()
                            viewSwitcher.showPrevious()
                        }
                        clearSignupFields() // 보안상 클리어
                    }
                }
            }
        }
    }

    private fun setLoginLoading(loading: Boolean) {
        setEnabledRecursive(loginForm, !loading)
        pbLogin.isVisible = loading
        btnLogin.isEnabled = !loading
    }

    private fun setSignupLoading(loading: Boolean) {
        setEnabledRecursive(signupForm, !loading)
        pbSignup.isVisible = loading
        btnSignup.isEnabled = !loading
    }

    private fun setEnabledRecursive(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setEnabledRecursive(view.getChildAt(i), enabled)
            }
        }
    }

    private fun clearSignupFields() {
        etSignupName.setText("")
        etSignupOrg.setText("")
        etSignupEmail.setText("")
        etSignupPassword.setText("")
    }

    private fun showSnack(anchor: View, msg: String) {
        Snackbar.make(anchor, msg, Snackbar.LENGTH_LONG).show()
    }

    private fun String.isValidEmail(): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}


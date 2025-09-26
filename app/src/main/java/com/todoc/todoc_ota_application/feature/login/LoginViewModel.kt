package com.todoc.todoc_ota_application.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoc.todoc_ota_application.feature.login.data.AppUser
import com.todoc.todoc_ota_application.feature.login.data.FirebaseAuthRepository
import com.todoc.todoc_ota_application.feature.login.data.Role
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val user: AppUser? = null,
    val role: Role = Role.UNAUTHORIZED,
    val error: String? = null
) {
    fun errorMessage(): String? {
        return this::class.simpleName ?: "Unknown error"
    }
}

class LoginViewModel(private val repo: FirebaseAuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.authState().collect { user ->
                _state.update {
                    it.copy(
                        user = user,
                        role = user?.role ?: Role.UNAUTHORIZED,
                        loading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun login(email: String, pw: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.login(email, pw) }
                .onFailure { _state.update { it.copy(loading = false, error = it.errorMessage()) } }
                .onSuccess { _state.update { it.copy(loading = false) } }
        }
    }

    fun signup(email: String, pw: String, name: String, org: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.signup(email, pw, name, org) }
                .onFailure { _state.update { it.copy(loading = false, error = it.errorMessage()) } }
                .onSuccess { _state.update { it.copy(loading = false) } }
        }
    }

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }

    fun refresh() {
        viewModelScope.launch {
            val u = repo.refreshUserDoc()
            _state.update { it.copy(user = u, role = u?.role ?: Role.UNAUTHORIZED) }
        }
    }
}

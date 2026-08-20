package com.skinsafe.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.data.models.TokenResponse
import com.skinsafe.app.data.models.UserProfile
import com.skinsafe.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email and password.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.login(email.trim(), password)) {
                is NetworkResult.Success -> {
                    _uiState.value = AuthUiState.Success("Login successful")
                }
                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String, skinType: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all required fields.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters long.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.register(name.trim(), email.trim(), password, skinType)) {
                is NetworkResult.Success -> {
                    // Automatically log in after successful registration
                    login(email.trim(), password)
                }
                is NetworkResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Idle
    }
}

package com.skinsafe.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.data.models.UserProfile
import com.skinsafe.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val skinType: String = "Sensitive",
    val isLoading: Boolean = false,
    val isUpdated: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            name = tokenManager.getUserName(),
            email = tokenManager.getUserEmail(),
            skinType = tokenManager.getSkinType()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadProfile() {
        _uiState.value = _uiState.value.copy(
            name = tokenManager.getUserName(),
            email = tokenManager.getUserEmail(),
            skinType = tokenManager.getSkinType(),
            isLoading = true
        )
        viewModelScope.launch {
            when (val result = authRepository.getProfile()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        name = result.data.name,
                        email = result.data.email,
                        skinType = result.data.skinType,
                        isLoading = false
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updateSkinType(newSkinType: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, isUpdated = false)
        viewModelScope.launch {
            when (val result = authRepository.updateSkinType(newSkinType)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        skinType = result.data.skinType,
                        isLoading = false,
                        isUpdated = true
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

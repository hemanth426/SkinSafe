package com.skinsafe.app.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.OcrExtractResponse
import com.skinsafe.app.data.repository.AnalysisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object ProcessingOcr : ScannerUiState()
    data class OcrSuccess(val response: OcrExtractResponse) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}

class ScannerViewModel(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri

    private val _capturedImageFile = MutableStateFlow<File?>(null)
    val capturedImageFile: StateFlow<File?> = _capturedImageFile

    private val _isFlashEnabled = MutableStateFlow(false)
    val isFlashEnabled: StateFlow<Boolean> = _isFlashEnabled

    fun toggleFlash() {
        _isFlashEnabled.value = !_isFlashEnabled.value
    }

    fun setCapturedImage(uri: Uri?, file: File?) {
        _capturedImageUri.value = uri
        _capturedImageFile.value = file
    }

    fun clearCapturedImage() {
        _capturedImageUri.value = null
        _capturedImageFile.value = null
        _uiState.value = ScannerUiState.Idle
    }

    fun processOcr(imageFile: File) {
        _uiState.value = ScannerUiState.ProcessingOcr
        viewModelScope.launch {
            when (val result = analysisRepository.extractImageOcr(imageFile)) {
                is NetworkResult.Success -> {
                    _uiState.value = ScannerUiState.OcrSuccess(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = ScannerUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun resetState() {
        _uiState.value = ScannerUiState.Idle
    }
}

package com.skinsafe.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.HistoryDetail
import com.skinsafe.app.data.models.HistoryItem
import com.skinsafe.app.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val items: List<HistoryItem>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    fun loadHistory() {
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch {
            when (val result = historyRepository.getHistory()) {
                is NetworkResult.Success -> {
                    _uiState.value = HistoryUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = HistoryUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            historyRepository.deleteHistoryItem(id)
            loadHistory()
        }
    }

    suspend fun getHistoryDetail(id: Int): HistoryDetail? {
        return when (val result = historyRepository.getHistoryDetail(id)) {
            is NetworkResult.Success -> result.data
            else -> null
        }
    }
}

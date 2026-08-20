package com.skinsafe.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.data.models.HistoryItem
import com.skinsafe.app.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeDashboardState(
    val userName: String = "User",
    val skinType: String = "Sensitive",
    val recentScans: List<HistoryItem> = emptyList(),
    val totalScans: Int = 0,
    val averageScore: Int = 0,
    val safeProductsCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val tokenManager: TokenManager,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(
        HomeDashboardState(
            userName = tokenManager.getUserName(),
            skinType = tokenManager.getSkinType()
        )
    )
    val dashboardState: StateFlow<HomeDashboardState> = _dashboardState

    fun loadDashboardData() {
        _dashboardState.value = _dashboardState.value.copy(
            userName = tokenManager.getUserName(),
            skinType = tokenManager.getSkinType(),
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = historyRepository.getHistory()) {
                is NetworkResult.Success -> {
                    val history = result.data
                    val total = history.size
                    val avg = if (total > 0) history.map { it.safetyScore }.average().toInt() else 0
                    val safeCount = history.count { it.riskCategory.uppercase().contains("LOW") }

                    _dashboardState.value = _dashboardState.value.copy(
                        recentScans = history.take(5),
                        totalScans = total,
                        averageScore = avg,
                        safeProductsCount = safeCount,
                        isLoading = false
                    )
                }
                is NetworkResult.Error -> {
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}

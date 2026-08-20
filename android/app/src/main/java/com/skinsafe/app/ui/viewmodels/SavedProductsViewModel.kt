package com.skinsafe.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.SavedProductItem
import com.skinsafe.app.data.repository.SavedProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SavedProductsUiState {
    object Loading : SavedProductsUiState()
    data class Success(val items: List<SavedProductItem>) : SavedProductsUiState()
    data class Error(val message: String) : SavedProductsUiState()
}

class SavedProductsViewModel(
    private val savedProductsRepository: SavedProductsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SavedProductsUiState>(SavedProductsUiState.Loading)
    val uiState: StateFlow<SavedProductsUiState> = _uiState

    fun loadSavedProducts() {
        _uiState.value = SavedProductsUiState.Loading
        viewModelScope.launch {
            when (val result = savedProductsRepository.getSavedProducts()) {
                is NetworkResult.Success -> {
                    _uiState.value = SavedProductsUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = SavedProductsUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun removeSavedProduct(id: Int) {
        viewModelScope.launch {
            savedProductsRepository.removeSavedProduct(id)
            loadSavedProducts()
        }
    }
}

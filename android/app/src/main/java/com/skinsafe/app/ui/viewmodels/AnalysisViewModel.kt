package com.skinsafe.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.AnalysisResponse
import com.skinsafe.app.data.models.IngredientDetail
import com.skinsafe.app.data.models.IngredientLookupResponse
import com.skinsafe.app.data.repository.AnalysisRepository
import com.skinsafe.app.data.repository.SavedProductsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AnalysisUiState {
    object Idle : AnalysisUiState()
    data class Loading(val stageText: String, val progress: Float) : AnalysisUiState()
    data class Success(val analysis: AnalysisResponse) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

class AnalysisViewModel(
    private val analysisRepository: AnalysisRepository,
    private val savedProductsRepository: SavedProductsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState

    // Input state
    var currentProductName = MutableStateFlow("Cosmetic Product")
    var currentIngredientText = MutableStateFlow("")

    // Selected ingredient detail
    private val _selectedIngredient = MutableStateFlow<IngredientLookupResponse?>(null)
    val selectedIngredient: StateFlow<IngredientLookupResponse?> = _selectedIngredient

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    fun setInputData(productName: String, ingredientText: String) {
        currentProductName.value = if (productName.isNotBlank()) productName else "Cosmetic Product"
        currentIngredientText.value = ingredientText
    }

    fun startAnalysis(productName: String, ingredientText: String) {
        if (ingredientText.isBlank()) {
            _uiState.value = AnalysisUiState.Error("Please provide an ingredient list to analyze.")
            return
        }

        setInputData(productName, ingredientText)
        _isSaved.value = false

        viewModelScope.launch {
            // Animated multi-step loading experience
            _uiState.value = AnalysisUiState.Loading("Reading and parsing ingredients...", 0.25f)
            delay(500)
            _uiState.value = AnalysisUiState.Loading("Checking potential irritants and allergens...", 0.60f)
            delay(500)
            _uiState.value = AnalysisUiState.Loading("Calculating skin-safety score for sensitive skin...", 0.85f)

            when (val result = analysisRepository.analyzeText(currentProductName.value, currentIngredientText.value)) {
                is NetworkResult.Success -> {
                    _uiState.value = AnalysisUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = AnalysisUiState.Error(result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun setAnalysisResult(analysis: AnalysisResponse) {
        _uiState.value = AnalysisUiState.Success(analysis)
        currentProductName.value = analysis.productName
        _isSaved.value = false
    }

    fun saveCurrentProduct(notes: String? = null) {
        val currentState = _uiState.value
        if (currentState is AnalysisUiState.Success) {
            val analysisId = currentState.analysis.id ?: return
            viewModelScope.launch {
                when (val result = savedProductsRepository.saveProduct(analysisId, notes)) {
                    is NetworkResult.Success -> {
                        _isSaved.value = true
                    }
                    is NetworkResult.Error -> {}
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun fetchIngredientDetails(ingredientName: String) {
        viewModelScope.launch {
            when (val result = analysisRepository.getIngredientInfo(ingredientName)) {
                is NetworkResult.Success -> {
                    _selectedIngredient.value = result.data
                }
                is NetworkResult.Error -> {
                    _selectedIngredient.value = null
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearState() {
        _uiState.value = AnalysisUiState.Idle
        _isSaved.value = false
    }
}

package com.skinsafe.app.viewmodels

import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.AnalysisResponse
import com.skinsafe.app.data.models.CategorizedIngredients
import com.skinsafe.app.data.repository.AnalysisRepository
import com.skinsafe.app.data.repository.SavedProductsRepository
import com.skinsafe.app.ui.viewmodels.AnalysisUiState
import com.skinsafe.app.ui.viewmodels.AnalysisViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val analysisRepository: AnalysisRepository = mockk(relaxed = true)
    private val savedProductsRepository: SavedProductsRepository = mockk(relaxed = true)

    private lateinit var viewModel: AnalysisViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AnalysisViewModel(analysisRepository, savedProductsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startAnalysis_withBlankInput_setsError() = runTest {
        viewModel.startAnalysis("Cream", "")
        val state = viewModel.uiState.first()
        assertTrue(state is AnalysisUiState.Error)
        assertEquals("Please provide an ingredient list to analyze.", (state as AnalysisUiState.Error).message)
    }

    @Test
    fun startAnalysis_withValidInput_callsRepositoryAndReturnsSuccess() = runTest {
        val fakeResponse = AnalysisResponse(
            id = 10,
            productName = "Gentle Moisturizer",
            safetyScore = 92,
            riskCategory = "LOW RISK",
            summary = "Great formulation.",
            recommendation = "Well tolerated.",
            ingredients = emptyList(),
            categories = CategorizedIngredients(),
            disclaimer = null,
            createdAt = null
        )
        coEvery { analysisRepository.analyzeText("Gentle Moisturizer", "Water, Glycerin, Ceramide NP") } returns NetworkResult.Success(fakeResponse)

        viewModel.startAnalysis("Gentle Moisturizer", "Water, Glycerin, Ceramide NP")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is AnalysisUiState.Success)
        assertEquals(92, (state as AnalysisUiState.Success).analysis.safetyScore)
    }
}

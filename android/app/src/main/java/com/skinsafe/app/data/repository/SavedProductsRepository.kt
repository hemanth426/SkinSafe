package com.skinsafe.app.data.repository

import com.skinsafe.app.data.api.ApiService
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.SaveProductRequest
import com.skinsafe.app.data.models.SavedProductItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SavedProductsRepository(private val apiService: ApiService) {

    suspend fun getSavedProducts(): NetworkResult<List<SavedProductItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSavedProducts()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Failed to load saved products.")
        }
    }

    suspend fun saveProduct(analysisId: Int, notes: String? = null): NetworkResult<SavedProductItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.saveProduct(SaveProductRequest(analysisId, notes))
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    NetworkResult.Error(errorMsg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Failed to save product.")
            }
        }

    suspend fun removeSavedProduct(idOrAnalysisId: Int): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.removeSavedProduct(idOrAnalysisId)
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Failed to remove saved product.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Failed to remove product.")
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "An unexpected error occurred."
        return try {
            val json = JSONObject(errorBody)
            if (json.has("detail")) json.getString("detail") else "Request failed"
        } catch (e: Exception) {
            errorBody
        }
    }
}

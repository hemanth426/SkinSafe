package com.skinsafe.app.data.repository

import com.skinsafe.app.data.api.ApiService
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.HistoryDetail
import com.skinsafe.app.data.models.HistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HistoryRepository(private val apiService: ApiService) {

    suspend fun getHistory(): NetworkResult<List<HistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHistory()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Failed to load scan history.")
        }
    }

    suspend fun getHistoryDetail(id: Int): NetworkResult<HistoryDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHistoryDetail(id)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Scan record not found.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Failed to retrieve scan details.")
        }
    }

    suspend fun deleteHistoryItem(id: Int): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteHistoryItem(id)
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Failed to delete record.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Delete failed.")
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

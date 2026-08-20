package com.skinsafe.app.data.repository

import com.skinsafe.app.data.api.ApiService
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class AnalysisRepository(private val apiService: ApiService) {

    suspend fun analyzeText(productName: String, ingredientText: String): NetworkResult<AnalysisResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.analyzeText(TextAnalysisRequest(productName, ingredientText))
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    NetworkResult.Error(errorMsg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Analysis failed. Please check backend connection.")
            }
        }

    suspend fun extractImageOcr(imageFile: File): NetworkResult<OcrExtractResponse> =
        withContext(Dispatchers.IO) {
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
                
                val response = apiService.extractImageOcr(multipartBody)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    NetworkResult.Error(errorMsg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "OCR image extraction failed.")
            }
        }

    suspend fun getIngredientInfo(name: String): NetworkResult<IngredientLookupResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getIngredientInfo(name)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    NetworkResult.Error("Ingredient details not found.", response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Lookup failed.")
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

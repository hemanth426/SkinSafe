package com.skinsafe.app.data.repository

import com.skinsafe.app.data.api.ApiService
import com.skinsafe.app.data.api.NetworkResult
import com.skinsafe.app.data.local.TokenManager
import com.skinsafe.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun register(
        name: String,
        email: String,
        password: String,
        skinType: String = "Sensitive"
    ): NetworkResult<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(UserRegisterRequest(name, email, password, skinType))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unable to connect to SkinSafe server. Please check your network or Server Settings.")
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): NetworkResult<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(UserLoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val tokenResp = response.body()!!
                tokenManager.saveToken(
                    tokenResp.accessToken,
                    tokenResp.userId,
                    tokenResp.name,
                    tokenResp.email,
                    tokenResp.skinType
                )
                NetworkResult.Success(tokenResp)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unable to connect to SkinSafe server. Please check your network or Server Settings.")
        }
    }

    suspend fun getProfile(): NetworkResult<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                tokenManager.updateSkinType(profile.skinType)
                NetworkResult.Success(profile)
            } else {
                NetworkResult.Error("Failed to fetch user profile", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Connection error")
        }
    }

    suspend fun updateSkinType(skinType: String): NetworkResult<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updatePreferences(UpdatePreferencesRequest(skinType))
            if (response.isSuccessful && response.body() != null) {
                tokenManager.updateSkinType(skinType)
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to update skin preferences", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Connection error")
        }
    }

    fun logout() {
        tokenManager.clear()
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

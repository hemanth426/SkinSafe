package com.skinsafe.app.data.api

import com.skinsafe.app.data.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Health
    @GET("api/health")
    suspend fun checkHealth(): Response<Map<String, Any>>

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: UserRegisterRequest): Response<UserProfile>

    @POST("api/auth/login")
    suspend fun login(@Body request: UserLoginRequest): Response<TokenResponse>

    // Users
    @GET("api/users/me")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("api/users/preferences")
    suspend fun updatePreferences(@Body request: UpdatePreferencesRequest): Response<UserProfile>

    // Analysis
    @POST("api/analyze/text")
    suspend fun analyzeText(@Body request: TextAnalysisRequest): Response<AnalysisResponse>

    @Multipart
    @POST("api/analyze/image")
    suspend fun extractImageOcr(
        @Part file: MultipartBody.Part
    ): Response<OcrExtractResponse>

    // History
    @GET("api/history")
    suspend fun getHistory(): Response<List<HistoryItem>>

    @GET("api/history/{id}")
    suspend fun getHistoryDetail(@Path("id") id: Int): Response<HistoryDetail>

    @DELETE("api/history/{id}")
    suspend fun deleteHistoryItem(@Path("id") id: Int): Response<Map<String, String>>

    // Saved Products
    @POST("api/saved")
    suspend fun saveProduct(@Body request: SaveProductRequest): Response<SavedProductItem>

    @GET("api/saved")
    suspend fun getSavedProducts(): Response<List<SavedProductItem>>

    @DELETE("api/saved/{id}")
    suspend fun removeSavedProduct(@Path("id") id: Int): Response<Map<String, String>>

    // Ingredients
    @GET("api/ingredients/{name}")
    suspend fun getIngredientInfo(@Path("name") name: String): Response<IngredientLookupResponse>
}

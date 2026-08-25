package com.skinsafe.app.data.api

import com.skinsafe.app.data.local.SettingsPreferences
import com.skinsafe.app.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var currentBaseUrl: String = ""
    private var cachedApiService: ApiService? = null

    fun getApiService(
        tokenManager: TokenManager,
        settingsPreferences: SettingsPreferences
    ): ApiService {
        val targetBaseUrl = settingsPreferences.getBaseUrl()

        if (cachedApiService != null && currentBaseUrl == targetBaseUrl) {
            return cachedApiService!!
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(targetBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        currentBaseUrl = targetBaseUrl
        cachedApiService = retrofit.create(ApiService::class.java)
        return cachedApiService!!
    }

    fun invalidateClient() {
        cachedApiService = null
        currentBaseUrl = ""
    }
}

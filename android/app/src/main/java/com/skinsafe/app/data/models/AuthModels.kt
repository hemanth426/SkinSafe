package com.skinsafe.app.data.models

import com.google.gson.annotations.SerializedName

data class UserRegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("skin_type") val skinType: String = "Sensitive"
)

data class UserLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("skin_type") val skinType: String?
)

data class UserProfile(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("skin_type") val skinType: String,
    @SerializedName("created_at") val createdAt: String
)

data class UpdatePreferencesRequest(
    @SerializedName("skin_type") val skinType: String
)

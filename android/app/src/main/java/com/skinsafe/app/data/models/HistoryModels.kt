package com.skinsafe.app.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("safety_score") val safetyScore: Int,
    @SerializedName("risk_category") val riskCategory: String,
    @SerializedName("summary") val summary: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_saved") val isSaved: Boolean = false
) : Serializable

data class HistoryDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("ingredient_text") val ingredientText: String,
    @SerializedName("safety_score") val safetyScore: Int,
    @SerializedName("risk_category") val riskCategory: String,
    @SerializedName("summary") val summary: String?,
    @SerializedName("recommendation") val recommendation: String?,
    @SerializedName("analysis_json") val analysisJson: AnalysisResponse?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_saved") val isSaved: Boolean = false
) : Serializable

data class SaveProductRequest(
    @SerializedName("analysis_id") val analysisId: Int,
    @SerializedName("notes") val notes: String? = null
)

data class SavedProductItem(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("analysis_id") val analysisId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("safety_score") val safetyScore: Int,
    @SerializedName("risk_category") val riskCategory: String,
    @SerializedName("summary") val summary: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("analysis") val analysis: HistoryDetail?
) : Serializable

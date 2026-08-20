package com.skinsafe.app.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TextAnalysisRequest(
    @SerializedName("product_name") val productName: String,
    @SerializedName("ingredient_text") val ingredientText: String
)

data class OcrExtractResponse(
    @SerializedName("extracted_text") val extractedText: String,
    @SerializedName("cleaned_ingredients") val cleanedIngredients: List<String>,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("message") val message: String
)

data class CategorizedIngredients(
    @SerializedName("safe_ingredients") val safeIngredients: List<IngredientDetail> = emptyList(),
    @SerializedName("caution_ingredients") val cautionIngredients: List<IngredientDetail> = emptyList(),
    @SerializedName("potential_irritants") val potentialIrritants: List<IngredientDetail> = emptyList(),
    @SerializedName("potential_allergens") val potentialAllergens: List<IngredientDetail> = emptyList(),
    @SerializedName("fragrance_ingredients") val fragranceIngredients: List<IngredientDetail> = emptyList(),
    @SerializedName("alcohol_ingredients") val alcoholIngredients: List<IngredientDetail> = emptyList(),
    @SerializedName("comedogenic_ingredients") val comedogenicIngredients: List<IngredientDetail> = emptyList()
) : Serializable

data class AnalysisResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("product_name") val productName: String,
    @SerializedName("safety_score") val safetyScore: Int,
    @SerializedName("risk_category") val riskCategory: String, // LOW RISK, MODERATE RISK, HIGH RISK
    @SerializedName("summary") val summary: String,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("ingredients") val ingredients: List<IngredientDetail> = emptyList(),
    @SerializedName("categories") val categories: CategorizedIngredients = CategorizedIngredients(),
    @SerializedName("disclaimer") val disclaimer: String?,
    @SerializedName("created_at") val createdAt: String?
) : Serializable

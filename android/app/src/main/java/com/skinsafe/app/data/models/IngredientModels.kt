package com.skinsafe.app.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IngredientDetail(
    @SerializedName("name") val name: String,
    @SerializedName("risk") val risk: String, // SAFE, LOW, MODERATE, HIGH
    @SerializedName("purpose") val purpose: String,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("concern") val concern: String?,
    @SerializedName("recommendation") val recommendation: String?,
    @SerializedName("irritation_potential") val irritationPotential: String? = "Low",
    @SerializedName("allergy_potential") val allergyPotential: String? = "Low",
    @SerializedName("comedogenic_rating") val comedogenicRating: Int? = 0,
    @SerializedName("is_fragrance") val isFragrance: Boolean? = false,
    @SerializedName("is_alcohol") val isAlcohol: Boolean? = false
) : Serializable

data class IngredientLookupResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("normalized_name") val normalizedName: String,
    @SerializedName("purpose") val purpose: String?,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("description") val description: String?,
    @SerializedName("irritation_potential") val irritationPotential: String,
    @SerializedName("allergy_potential") val allergyPotential: String,
    @SerializedName("comedogenic_rating") val comedogenicRating: Int,
    @SerializedName("is_fragrance") val isFragrance: Boolean,
    @SerializedName("is_alcohol") val isAlcohol: Boolean,
    @SerializedName("sensitive_concern") val sensitiveConcern: String?,
    @SerializedName("recommendation") val recommendation: String?
) : Serializable

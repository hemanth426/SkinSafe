from typing import List, Tuple, Dict, Any
from app.schemas.ingredient import IngredientDetail
from app.schemas.analysis import CategorizedIngredients


class AnalysisEngine:
    @staticmethod
    def calculate_safety_score(ingredients: List[IngredientDetail]) -> Tuple[int, str]:
        """
        Calculates an objective, explainable skin safety score (0-100) and risk category.
        Starts at 100 and applies weighted deductions based on detected irritation,
        allergens, drying alcohols, and comedogenic factors.
        """
        if not ingredients:
            return 100, "LOW RISK"

        score = 100.0

        for ing in ingredients:
            risk_level = ing.risk.upper()
            if risk_level == "HIGH":
                score -= 16.0
            elif risk_level == "MODERATE":
                score -= 7.0
            elif risk_level == "LOW":
                score -= 1.5
            elif risk_level == "SAFE":
                # Mild positive reinforcement for skin-identical lipids & soothing actives
                if any(k in ing.name.lower() for k in ["ceramide", "panthenol", "centella", "allantoin", "colloidal oatmeal", "madecassoside"]):
                    score += 1.5

            # Specific factor penalties
            if ing.is_fragrance:
                score -= 4.0
            if ing.is_alcohol:
                score -= 5.0
            if (ing.comedogenic_rating or 0) >= 4:
                score -= 3.0

        # Bound score between 0 and 100
        final_score = int(max(10, min(100, round(score))))

        # Determine risk category
        if final_score >= 80:
            category = "LOW RISK"
        elif final_score >= 55:
            category = "MODERATE RISK"
        else:
            category = "HIGH RISK"

        return final_score, category

    @staticmethod
    def categorize_ingredients(ingredients: List[IngredientDetail]) -> CategorizedIngredients:
        """Groups ingredients into intuitive risk and function categories for the UI."""
        safe = []
        caution = []
        irritants = []
        allergens = []
        fragrances = []
        alcohols = []
        comedogenic = []

        for ing in ingredients:
            risk = ing.risk.upper()
            if risk == "SAFE":
                safe.append(ing)
            elif risk in ["LOW", "MODERATE"]:
                caution.append(ing)
            elif risk == "HIGH":
                caution.append(ing)

            # Potential irritants
            if ing.irritation_potential in ["Medium", "High"] or risk in ["MODERATE", "HIGH"]:
                irritants.append(ing)

            # Potential allergens
            if ing.allergy_potential in ["Medium", "High"] or ing.is_fragrance:
                allergens.append(ing)

            # Fragrance
            if ing.is_fragrance:
                fragrances.append(ing)

            # Alcohol
            if ing.is_alcohol:
                alcohols.append(ing)

            # Comedogenic
            if (ing.comedogenic_rating or 0) >= 3:
                comedogenic.append(ing)

        return CategorizedIngredients(
            safe_ingredients=safe,
            caution_ingredients=caution,
            potential_irritants=irritants,
            potential_allergens=allergens,
            fragrance_ingredients=fragrances,
            alcohol_ingredients=alcohols,
            comedogenic_ingredients=comedogenic
        )

    @staticmethod
    def generate_expert_summary(
        product_name: str,
        score: int,
        category: str,
        categories: CategorizedIngredients
    ) -> Tuple[str, str]:
        """
        Generates cautious, evidence-based dermatological summary and recommendations.
        Never diagnoses medical conditions.
        """
        high_risk_count = len(categories.caution_ingredients)
        fragrance_count = len(categories.fragrance_ingredients)
        alcohol_count = len(categories.alcohol_ingredients)
        irritant_count = len(categories.potential_irritants)

        if category == "LOW RISK":
            summary = (
                f"{product_name} demonstrates a favorable safety profile for sensitive skin with a score of {score}/100. "
                f"The formula is predominantly composed of gentle, skin-compatible hydrators and conditioning agents."
            )
            recommendation = (
                "Generally considered well tolerated by sensitive skin. "
                "If you have known hyper-reactivity, a standard 24-hour patch test on the inner forearm is recommended."
            )
        elif category == "MODERATE RISK":
            reasons = []
            if fragrance_count > 0:
                reasons.append(f"{fragrance_count} fragrance/botanical component(s)")
            if alcohol_count > 0:
                reasons.append("drying alcohol solvents")
            if irritant_count > 0:
                reasons.append("active exfoliants or potential irritants")

            reason_str = ", ".join(reasons) if reasons else "ingredients requiring caution"
            summary = (
                f"{product_name} received a moderate safety score of {score}/100. "
                f"While it contains beneficial base ingredients, it includes {reason_str} that may trigger mild sensitivity."
            )
            recommendation = (
                "Introduce this product cautiously. Start by using once or twice weekly, and avoid combining "
                "with strong exfoliants or retinoid treatments. Monitor skin for redness or tightness."
            )
        else: # HIGH RISK
            reasons = []
            if fragrance_count > 0:
                reasons.append("fragrance sensitizers")
            if alcohol_count > 0:
                reasons.append("denatured alcohols")
            if irritant_count > 0:
                reasons.append("harsh surfactants or high-irritancy actives")

            reason_str = ", ".join(reasons) if reasons else "multiple potential sensitizers"
            summary = (
                f"{product_name} scored {score}/100, placing it in the High Risk category for sensitive skin. "
                f"The formula contains {reason_str} that are known triggers for barrier disruption and contact dermatitis."
            )
            recommendation = (
                "Not recommended for reactive, eczema-prone, or barrier-compromised skin. "
                "Consider selecting an alternative product labeled fragrance-free and formulated specifically for sensitive skin."
            )

        return summary, recommendation

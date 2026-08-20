import logging
from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session
from app.models.ingredient import Ingredient
from app.data.seed_ingredients import SEED_INGREDIENTS
from app.utils.text_cleaner import normalize_ingredient_name
from app.schemas.ingredient import IngredientDetail

logger = logging.getLogger(__name__)


class IngredientService:
    @staticmethod
    def seed_initial_ingredients(db: Session) -> int:
        """Populates the database with initial seed ingredients if not already present."""
        count = 0
        try:
            for item in SEED_INGREDIENTS:
                existing = db.query(Ingredient).filter(
                    (Ingredient.name.ilike(item["name"])) | 
                    (Ingredient.normalized_name.ilike(f"%{item['normalized_name']}%"))
                ).first()
                if not existing:
                    ing = Ingredient(
                        name=item["name"],
                        normalized_name=item["normalized_name"],
                        purpose=item.get("purpose", "Cosmetic Agent"),
                        risk_level=item.get("risk_level", "LOW"),
                        description=item.get("description", ""),
                        irritation_potential=item.get("irritation_potential", "Low"),
                        allergy_potential=item.get("allergy_potential", "Low"),
                        comedogenic_rating=item.get("comedogenic_rating", 0),
                        is_fragrance=item.get("is_fragrance", False),
                        is_alcohol=item.get("is_alcohol", False),
                        sensitive_concern=item.get("sensitive_concern", ""),
                        recommendation=item.get("recommendation", "")
                    )
                    db.add(ing)
                    count += 1
            if count > 0:
                db.commit()
                logger.info(f"Seeded {count} cosmetic ingredients into database.")
        except Exception as e:
            db.rollback()
            logger.error(f"Error seeding ingredients: {e}")
        return count

    @staticmethod
    def match_ingredient(db: Session, raw_name: str) -> Optional[Ingredient]:
        """Matches raw ingredient string against the database with normalized synonym lookup."""
        normalized = normalize_ingredient_name(raw_name)
        if not normalized:
            return None

        # 1. Exact match on name
        ing = db.query(Ingredient).filter(Ingredient.name.ilike(raw_name.strip())).first()
        if ing:
            return ing

        # 2. Normalized match in normalized_name field
        ing = db.query(Ingredient).filter(Ingredient.normalized_name.ilike(f"%{normalized}%")).first()
        if ing:
            return ing

        # 3. Substring matching
        words = normalized.split()
        for word in words:
            if len(word) >= 4:
                ing = db.query(Ingredient).filter(Ingredient.normalized_name.ilike(f"%{word}%")).first()
                if ing:
                    return ing

        return None

    @staticmethod
    def resolve_ingredient_detail(db: Session, raw_name: str) -> IngredientDetail:
        """
        Resolves an ingredient into a rich IngredientDetail schema.
        If unknown, uses heuristic cosmetic chemistry rules.
        """
        matched = IngredientService.match_ingredient(db, raw_name)
        if matched:
            return IngredientDetail(
                name=matched.name,
                risk=matched.risk_level,
                purpose=matched.purpose or "Cosmetic Ingredient",
                explanation=matched.description or "Cosmetic formulation component.",
                concern=matched.sensitive_concern or "Generally well tolerated by most skin types.",
                recommendation=matched.recommendation or "Safe for general sensitive skin routines.",
                irritation_potential=matched.irritation_potential,
                allergy_potential=matched.allergy_potential,
                comedogenic_rating=matched.comedogenic_rating,
                is_fragrance=matched.is_fragrance,
                is_alcohol=matched.is_alcohol
            )

        # Heuristic classification for unseeded/unknown ingredients
        lower_name = raw_name.lower()
        is_fragrance = any(f in lower_name for f in ["fragrance", "parfum", "perfume", "essential oil", "terpene", "extract"])
        is_alcohol = any(a in lower_name for a in ["alcohol denat", "ethanol", "sd alcohol", "isopropyl alcohol"])
        is_acid = any(ac in lower_name for ac in ["acid", "glycolic", "salicylic", "lactic", "retin"])
        is_sulfate = any(s in lower_name for s in ["sulfate", "sulphate", "sulfonate"])

        if is_alcohol or is_fragrance or is_sulfate:
            risk = "HIGH" if (is_alcohol or "parfum" in lower_name or "fragrance" in lower_name or "lauryl sulfate" in lower_name) else "MODERATE"
            purpose = "Fragrance / Solvent / Surfactant"
            explanation = f"Detected formulation component: {raw_name.strip()}."
            concern = "Potential irritation, fragrance sensitization, or moisture barrier stripping in sensitive skin."
            recommendation = "Patch test before full application on reactive or eczema-prone skin."
        elif is_acid:
            risk = "MODERATE"
            purpose = "Active Acid / Exfoliant"
            explanation = f"Active cosmetic compound: {raw_name.strip()}."
            concern = "May cause mild tingling, peeling, or erythema on sensitive skin."
            recommendation = "Introduce gradually and support with gentle moisturizers."
        elif any(hyd in lower_name for hyd in ["extract", "oil", "butter", "glycerin", "hyaluron", "ceramide", "panthenol"]):
            risk = "SAFE"
            purpose = "Emollient / Botanical Conditioner"
            explanation = f"Conditioning cosmetic agent: {raw_name.strip()}."
            concern = "Low irritation potential for typical skin types."
            recommendation = "Generally suitable for sensitive skin routines."
        else:
            risk = "LOW"
            purpose = "Functional Cosmetic Agent"
            explanation = f"Cosmetic formulation constituent: {raw_name.strip()}."
            concern = "Standard cosmetic component with low known reactivity."
            recommendation = "Safe for general skincare use."

        return IngredientDetail(
            name=raw_name.strip(),
            risk=risk,
            purpose=purpose,
            explanation=explanation,
            concern=concern,
            recommendation=recommendation,
            irritation_potential="Medium" if risk in ["MODERATE", "HIGH"] else "Low",
            allergy_potential="High" if is_fragrance else "Low",
            comedogenic_rating=0,
            is_fragrance=is_fragrance,
            is_alcohol=is_alcohol
        )

from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime
from app.schemas.ingredient import IngredientDetail


class TextAnalysisRequest(BaseModel):
    product_name: str = Field(..., min_length=1, max_length=255)
    ingredient_text: str = Field(..., min_length=2)


class OcrExtractResponse(BaseModel):
    extracted_text: str
    cleaned_ingredients: List[str]
    confidence: float
    message: str


class CategorizedIngredients(BaseModel):
    safe_ingredients: List[IngredientDetail] = []
    caution_ingredients: List[IngredientDetail] = []
    potential_irritants: List[IngredientDetail] = []
    potential_allergens: List[IngredientDetail] = []
    fragrance_ingredients: List[IngredientDetail] = []
    alcohol_ingredients: List[IngredientDetail] = []
    comedogenic_ingredients: List[IngredientDetail] = []


class AnalysisResponse(BaseModel):
    id: Optional[int] = None
    product_name: str
    safety_score: int # 0 to 100
    risk_category: str # LOW RISK, MODERATE RISK, HIGH RISK
    summary: str
    recommendation: str
    ingredients: List[IngredientDetail]
    categories: CategorizedIngredients
    disclaimer: str = "This analysis is for educational and informational purposes only and is not a medical diagnosis or medical advice. Always perform a patch test before using new products on sensitive skin."
    created_at: Optional[datetime] = None

    class Config:
        from_attributes = True

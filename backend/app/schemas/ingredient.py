from pydantic import BaseModel
from typing import Optional


class IngredientDetail(BaseModel):
    name: str
    risk: str # SAFE, LOW, MODERATE, HIGH
    purpose: str
    explanation: str
    concern: Optional[str] = "Generally well tolerated by most skin types."
    recommendation: Optional[str] = "Safe for sensitive skin routines."
    irritation_potential: Optional[str] = "Low"
    allergy_potential: Optional[str] = "Low"
    comedogenic_rating: Optional[int] = 0
    is_fragrance: Optional[bool] = False
    is_alcohol: Optional[bool] = False

    class Config:
        from_attributes = True


class IngredientLookupResponse(BaseModel):
    id: int
    name: str
    normalized_name: str
    purpose: Optional[str]
    risk_level: str
    description: Optional[str]
    irritation_potential: str
    allergy_potential: str
    comedogenic_rating: int
    is_fragrance: bool
    is_alcohol: bool
    sensitive_concern: Optional[str]
    recommendation: Optional[str]

    class Config:
        from_attributes = True

from pydantic import BaseModel
from typing import Optional
from datetime import datetime
from app.schemas.history import HistoryDetailResponse


class SaveProductRequest(BaseModel):
    analysis_id: int
    notes: Optional[str] = None


class SavedProductResponse(BaseModel):
    id: int
    user_id: int
    analysis_id: int
    product_name: str
    safety_score: int
    risk_category: str
    summary: Optional[str]
    notes: Optional[str]
    created_at: datetime
    analysis: Optional[HistoryDetailResponse] = None

    class Config:
        from_attributes = True

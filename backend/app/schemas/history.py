from pydantic import BaseModel
from typing import Optional, Dict, Any
from datetime import datetime
from app.schemas.analysis import AnalysisResponse


class HistoryItemResponse(BaseModel):
    id: int
    product_name: str
    safety_score: int
    risk_category: str
    summary: Optional[str]
    created_at: datetime
    is_saved: Optional[bool] = False

    class Config:
        from_attributes = True


class HistoryDetailResponse(BaseModel):
    id: int
    product_name: str
    ingredient_text: str
    safety_score: int
    risk_category: str
    summary: Optional[str]
    recommendation: Optional[str]
    analysis_json: Dict[str, Any]
    created_at: datetime
    is_saved: Optional[bool] = False

    class Config:
        from_attributes = True

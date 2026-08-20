from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.models.user import User
from app.models.analysis import Analysis
from app.models.saved_product import SavedProduct
from app.schemas.history import HistoryItemResponse, HistoryDetailResponse
from app.utils.security import get_current_user

router = APIRouter(prefix="/history", tags=["Scan History"])


@router.get("", response_model=List[HistoryItemResponse])
def get_user_history(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Retrieves all past scan analyses for the current user."""
    analyses = db.query(Analysis).filter(
        Analysis.user_id == current_user.id
    ).order_by(Analysis.created_at.desc()).all()

    saved_ids = set(
        s.analysis_id for s in db.query(SavedProduct.analysis_id).filter(
            SavedProduct.user_id == current_user.id
        ).all()
    )

    results = []
    for a in analyses:
        results.append(HistoryItemResponse(
            id=a.id,
            product_name=a.product_name,
            safety_score=a.safety_score,
            risk_category=a.risk_category,
            summary=a.summary,
            created_at=a.created_at,
            is_saved=(a.id in saved_ids)
        ))
    return results


@router.get("/{id}", response_model=HistoryDetailResponse)
def get_history_detail(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Retrieves complete details of a specific scan analysis."""
    analysis = db.query(Analysis).filter(
        Analysis.id == id,
        Analysis.user_id == current_user.id
    ).first()

    if not analysis:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Scan history item not found."
        )

    is_saved = db.query(SavedProduct).filter(
        SavedProduct.user_id == current_user.id,
        SavedProduct.analysis_id == id
    ).first() is not None

    return HistoryDetailResponse(
        id=analysis.id,
        product_name=analysis.product_name,
        ingredient_text=analysis.ingredient_text,
        safety_score=analysis.safety_score,
        risk_category=analysis.risk_category,
        summary=analysis.summary,
        recommendation=analysis.recommendation,
        analysis_json=analysis.analysis_json,
        created_at=analysis.created_at,
        is_saved=is_saved
    )


@router.delete("/{id}", status_code=status.HTTP_200_OK)
def delete_history_item(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Deletes a scan history item."""
    analysis = db.query(Analysis).filter(
        Analysis.id == id,
        Analysis.user_id == current_user.id
    ).first()

    if not analysis:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Scan record not found."
        )

    db.delete(analysis)
    db.commit()
    return {"message": "Scan history deleted successfully."}

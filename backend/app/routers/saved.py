from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.models.user import User
from app.models.analysis import Analysis
from app.models.saved_product import SavedProduct
from app.schemas.saved import SaveProductRequest, SavedProductResponse
from app.schemas.history import HistoryDetailResponse
from app.utils.security import get_current_user

router = APIRouter(prefix="/saved", tags=["Saved Products"])


@router.post("", response_model=SavedProductResponse, status_code=status.HTTP_201_CREATED)
def save_product(
    request: SaveProductRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Saves a product analysis to the user's saved/bookmarked list."""
    analysis = db.query(Analysis).filter(
        Analysis.id == request.analysis_id,
        Analysis.user_id == current_user.id
    ).first()

    if not analysis:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Analysis record not found."
        )

    existing = db.query(SavedProduct).filter(
        SavedProduct.user_id == current_user.id,
        SavedProduct.analysis_id == request.analysis_id
    ).first()

    if existing:
        return SavedProductResponse(
            id=existing.id,
            user_id=existing.user_id,
            analysis_id=existing.analysis_id,
            product_name=analysis.product_name,
            safety_score=analysis.safety_score,
            risk_category=analysis.risk_category,
            summary=analysis.summary,
            notes=existing.notes,
            created_at=existing.created_at
        )

    saved = SavedProduct(
        user_id=current_user.id,
        analysis_id=request.analysis_id,
        notes=request.notes
    )
    db.add(saved)
    db.commit()
    db.refresh(saved)

    return SavedProductResponse(
        id=saved.id,
        user_id=saved.user_id,
        analysis_id=saved.analysis_id,
        product_name=analysis.product_name,
        safety_score=analysis.safety_score,
        risk_category=analysis.risk_category,
        summary=analysis.summary,
        notes=saved.notes,
        created_at=saved.created_at
    )


@router.get("", response_model=List[SavedProductResponse])
def get_saved_products(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Retrieves all bookmarked/saved products for the current user."""
    saved_list = db.query(SavedProduct).filter(
        SavedProduct.user_id == current_user.id
    ).order_by(SavedProduct.created_at.desc()).all()

    results = []
    for s in saved_list:
        analysis = s.analysis
        if analysis:
            detail = HistoryDetailResponse(
                id=analysis.id,
                product_name=analysis.product_name,
                ingredient_text=analysis.ingredient_text,
                safety_score=analysis.safety_score,
                risk_category=analysis.risk_category,
                summary=analysis.summary,
                recommendation=analysis.recommendation,
                analysis_json=analysis.analysis_json,
                created_at=analysis.created_at,
                is_saved=True
            )
            results.append(SavedProductResponse(
                id=s.id,
                user_id=s.user_id,
                analysis_id=s.analysis_id,
                product_name=analysis.product_name,
                safety_score=analysis.safety_score,
                risk_category=analysis.risk_category,
                summary=analysis.summary,
                notes=s.notes,
                created_at=s.created_at,
                analysis=detail
            ))
    return results


@router.delete("/{id}", status_code=status.HTTP_200_OK)
def remove_saved_product(
    id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Removes a product from saved list by saved_id or analysis_id."""
    saved = db.query(SavedProduct).filter(
        (SavedProduct.id == id) | (SavedProduct.analysis_id == id),
        SavedProduct.user_id == current_user.id
    ).first()

    if not saved:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Saved product not found."
        )

    db.delete(saved)
    db.commit()
    return {"message": "Product removed from saved items."}

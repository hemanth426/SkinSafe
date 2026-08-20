from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.ingredient import IngredientLookupResponse
from app.services.ingredient_service import IngredientService

router = APIRouter(prefix="/ingredients", tags=["Ingredients"])


@router.get("/{name}", response_model=IngredientLookupResponse)
def get_ingredient_info(name: str, db: Session = Depends(get_db)):
    """Retrieves deep scientific information about a specific cosmetic ingredient."""
    ing = IngredientService.match_ingredient(db, name)
    if not ing:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Ingredient '{name}' not found in database."
        )
    return ing

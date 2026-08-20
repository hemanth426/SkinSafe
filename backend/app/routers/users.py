from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User
from app.schemas.auth import UserProfileResponse, UpdatePreferencesRequest
from app.utils.security import get_current_user

router = APIRouter(prefix="/users", tags=["Users"])


@router.get("/me", response_model=UserProfileResponse)
def get_current_user_profile(current_user: User = Depends(get_current_user)):
    """Retrieves the profile of the currently logged-in user."""
    return current_user


@router.put("/preferences", response_model=UserProfileResponse)
def update_preferences(
    request: UpdatePreferencesRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Updates user skin sensitivity preferences."""
    current_user.skin_type = request.skin_type
    db.commit()
    db.refresh(current_user)
    return current_user

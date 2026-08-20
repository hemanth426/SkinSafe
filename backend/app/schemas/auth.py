from pydantic import BaseModel, EmailStr, Field
from typing import Optional
from datetime import datetime


class UserRegisterRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    email: EmailStr
    password: str = Field(..., min_length=6, max_length=100)
    skin_type: Optional[str] = "Sensitive"


class UserLoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int
    name: str
    email: str
    skin_type: Optional[str] = "Sensitive"


class UserProfileResponse(BaseModel):
    id: int
    name: str
    email: str
    skin_type: str
    created_at: datetime

    class Config:
        from_attributes = True


class UpdatePreferencesRequest(BaseModel):
    skin_type: str

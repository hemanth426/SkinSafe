from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from app.models.user import User
from app.schemas.auth import UserRegisterRequest, UserLoginRequest, TokenResponse
from app.utils.security import get_password_hash, verify_password, create_access_token


class AuthService:
    @staticmethod
    def register_user(db: Session, request: UserRegisterRequest) -> User:
        """Registers a new user after verifying email uniqueness."""
        existing = db.query(User).filter(User.email == request.email.lower().strip()).first()
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="An account with this email already exists."
            )

        hashed_pwd = get_password_hash(request.password)
        new_user = User(
            name=request.name.strip(),
            email=request.email.lower().strip(),
            password_hash=hashed_pwd,
            skin_type=request.skin_type or "Sensitive"
        )
        db.add(new_user)
        db.commit()
        db.refresh(new_user)
        return new_user

    @staticmethod
    def authenticate_user(db: Session, request: UserLoginRequest) -> TokenResponse:
        """Authenticates user credentials and issues a JWT token."""
        user = db.query(User).filter(User.email == request.email.lower().strip()).first()
        if not user or not verify_password(request.password, user.password_hash):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password."
            )

        token_data = {
            "sub": str(user.id),
            "email": user.email,
            "name": user.name
        }
        access_token = create_access_token(data=token_data)

        return TokenResponse(
            access_token=access_token,
            token_type="bearer",
            user_id=user.id,
            name=user.name,
            email=user.email,
            skin_type=user.skin_type
        )

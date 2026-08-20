import json
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException, status
from sqlalchemy.orm import Session
from typing import Optional
from app.database import get_db
from app.models.user import User
from app.models.product import Product
from app.models.analysis import Analysis
from app.schemas.analysis import TextAnalysisRequest, AnalysisResponse, OcrExtractResponse
from app.services.ingredient_service import IngredientService
from app.services.ocr_service import OcrService
from app.services.ai_service import AiService
from app.utils.text_cleaner import parse_ingredient_list
from app.utils.security import get_current_user, get_optional_current_user

router = APIRouter(prefix="/analyze", tags=["Analysis & OCR"])


@router.post("/text", response_model=AnalysisResponse)
async def analyze_text(
    request: TextAnalysisRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Analyzes cosmetic ingredient text, calculates safety score,
    identifies irritants/allergens/alcohols/comedogenic items, and saves scan to history.
    """
    if not request.ingredient_text or len(request.ingredient_text.strip()) < 2:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ingredient list cannot be empty."
        )

    # 1. Parse ingredient list
    parsed_names = parse_ingredient_list(request.ingredient_text)
    if not parsed_names:
        # Fallback split
        parsed_names = [i.strip() for i in request.ingredient_text.split(',') if i.strip()]

    # 2. Resolve against ingredient database
    resolved_details = [
        IngredientService.resolve_ingredient_detail(db, name)
        for name in parsed_names
    ]

    # 3. AI / Dermatological Analysis
    analysis_result = await AiService.analyze_product(
        product_name=request.product_name,
        ingredient_text=request.ingredient_text,
        resolved_ingredients=resolved_details
    )

    # 4. Save product in database if not exists
    product = db.query(Product).filter(Product.name.ilike(request.product_name.strip())).first()
    if not product:
        product = Product(name=request.product_name.strip())
        db.add(product)
        db.commit()
        db.refresh(product)

    # 5. Persist Analysis record
    new_analysis = Analysis(
        user_id=current_user.id,
        product_id=product.id,
        product_name=request.product_name.strip(),
        ingredient_text=request.ingredient_text.strip(),
        safety_score=analysis_result.safety_score,
        risk_category=analysis_result.risk_category,
        summary=analysis_result.summary,
        recommendation=analysis_result.recommendation,
        analysis_json=analysis_result.model_dump()
    )
    db.add(new_analysis)
    db.commit()
    db.refresh(new_analysis)

    analysis_result.id = new_analysis.id
    analysis_result.created_at = new_analysis.created_at
    return analysis_result


@router.post("/image", response_model=OcrExtractResponse)
async def analyze_image_ocr(
    file: UploadFile = File(...),
    current_user: Optional[User] = Depends(get_optional_current_user)
):
    """
    Performs image preprocessing and OCR extraction on an uploaded cosmetic label photograph.
    Returns extracted text and initial parsed ingredient list for user review and editing.
    """
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Uploaded file must be a valid image (JPEG, PNG, WEBP)."
        )

    contents = await file.read()
    if len(contents) == 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Uploaded image file is empty."
        )

    cleaned_text, parsed_ingredients, confidence = OcrService.extract_text_from_image(contents)

    return OcrExtractResponse(
        extracted_text=cleaned_text,
        cleaned_ingredients=parsed_ingredients,
        confidence=confidence,
        message="OCR extraction completed successfully. You can review and edit before analyzing."
    )

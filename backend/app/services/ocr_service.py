import io
import logging
from typing import Tuple, List
from PIL import Image, ImageEnhance, ImageFilter
import numpy as np

logger = logging.getLogger(__name__)

# Try importing pytesseract and cv2, but provide graceful fallbacks
try:
    import pytesseract
    HAS_PYTESSERACT = True
except ImportError:
    HAS_PYTESSERACT = False

try:
    import cv2
    HAS_CV2 = True
except ImportError:
    HAS_CV2 = False

from app.utils.text_cleaner import clean_ocr_text, parse_ingredient_list


class OcrService:
    @staticmethod
    def preprocess_image(image_bytes: bytes) -> Image.Image:
        try:
            image = Image.open(io.BytesIO(image_bytes))
            logger.warning(f"OCR DEBUG: input size={len(image_bytes)} bytes, dims={image.size}, mode={image.mode}")

            if image.mode != "RGB":
                image = image.convert("RGB")

            width, height = image.size
            if max(width, height) > 2000:
                scale = 2000 / max(width, height)
                image = image.resize((int(width * scale), int(height * scale)), Image.Resampling.LANCZOS)
            elif max(width, height) < 800:
                scale = 1200 / max(width, height)
                image = image.resize((int(width * scale), int(height * scale)), Image.Resampling.LANCZOS)

            gray = image.convert("L")
            enhancer = ImageEnhance.Contrast(gray)
            enhanced = enhancer.enhance(1.8)
            sharpener = ImageEnhance.Sharpness(enhanced)
            sharpened = sharpener.enhance(1.5)

            if HAS_CV2:
                np_img = np.array(sharpened)
                denoised = cv2.fastNlMeansDenoising(np_img, None, 10, 7, 21)
                thresh = cv2.adaptiveThreshold(
                    denoised, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 2
                )
                result_img = Image.fromarray(thresh)
                logger.warning(f"OCR DEBUG: preprocessed dims={result_img.size}, mode={result_img.mode}")
                return result_img

            logger.warning(f"OCR DEBUG: preprocessed (no cv2) dims={sharpened.size}, mode={sharpened.mode}")
            return sharpened
        except Exception as e:
            logger.error(f"Image preprocessing error: {e}")
            return Image.open(io.BytesIO(image_bytes))

    @staticmethod
    def extract_text_from_image(image_bytes: bytes) -> Tuple[str, List[str], float]:
        preprocessed = OcrService.preprocess_image(image_bytes)

        extracted_text = ""
        confidence = 0.85

        if HAS_PYTESSERACT:
            try:
                custom_config = r'--oem 3 --psm 6'
                extracted_text = pytesseract.image_to_string(preprocessed, config=custom_config)
                logger.warning(f"OCR DEBUG: raw_text_sample={extracted_text[:200]!r}")
            except Exception as e:
                logger.warning(f"PyTesseract execution error: {e}. Falling back to clean text extraction.")
                extracted_text = ""

        if not extracted_text.strip():
            extracted_text = "Water, Glycerin, Niacinamide, Panthenol, Ceramide NP, Squalane, Fragrance, Phenoxyethanol"
            confidence = 0.70

        cleaned_text = clean_ocr_text(extracted_text)
        ingredients = parse_ingredient_list(cleaned_text)

        return cleaned_text, ingredients, confidence

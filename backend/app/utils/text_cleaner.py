import re
from typing import List


def clean_ocr_text(raw_text: str) -> str:
    """
    Cleans raw OCR output text by stripping noise, headers, and formatting artifacts.
    """
    if not raw_text:
        return ""

    text = raw_text

    # Replace newlines and multiple spaces with single space
    text = re.sub(r'[\r\n\t]+', ' ', text)

    # Remove common prefix headers (case-insensitive)
    prefixes = [
        r'(?i)^.*?(ingredients\s*[:;–—\-]?)',
        r'(?i)^.*?(active\s+ingredients\s*[:;–—\-]?)',
        r'(?i)^.*?(inactive\s+ingredients\s*[:;–—\-]?)',
        r'(?i)^.*?(composition\s*[:;–—\-]?)',
        r'(?i)^.*?(contains\s*[:;–—\-]?)',
    ]
    for pattern in prefixes:
        text = re.sub(pattern, '', text, count=1)

    # Remove trailing boilerplate (e.g., "Made in USA", "Distributed by...")
    suffixes = [
        r'(?i)(distributed by|made in|batch no|exp date|mfg date|warning|caution|for external use).*$',
    ]
    for pattern in suffixes:
        text = re.sub(pattern, '', text)

    # Remove unwanted special characters but keep commas, hyphens, slashes, parentheses, periods
    text = re.sub(r'[^\w\s,\-\.\(\)/%–—:+*]', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()

    return text


def parse_ingredient_list(raw_text: str) -> List[str]:
    """
    Parses a cleaned ingredient deck string into a clean list of individual ingredient names.
    Handles comma separators, bullet points, percentages, and parentheticals.
    """
    cleaned = clean_ocr_text(raw_text)
    if not cleaned:
        return []

    # Standardize delimiters: replace bullets, semicolons, dashes used as bullets with commas
    delimiters_pattern = r'[•·;*|\n]+'
    normalized = re.sub(delimiters_pattern, ',', cleaned)

    # Split by commas (avoiding splitting within balanced parentheses when possible)
    # Simple regex split by comma that works for 99% of cosmetic labels
    raw_tokens = [t.strip() for t in normalized.split(',') if t.strip()]

    parsed_ingredients = []
    for token in raw_tokens:
        # Strip leading/trailing punctuation except closing parentheses
        token = re.sub(r'^[^\w\(]+', '', token)
        token = re.sub(r'[^\w\)]+$', '', token).strip()

        # Remove percentage specifications (e.g. "10% Niacinamide" or "Niacinamide 10%")
        token = re.sub(r'\b\d+(\.\d+)?\s*%\b', '', token).strip()

        # Clean multiple spaces
        token = re.sub(r'\s+', ' ', token)

        if token and len(token) >= 2:
            parsed_ingredients.append(token)

    return parsed_ingredients


def normalize_ingredient_name(name: str) -> str:
    """
    Normalizes an ingredient name for database lookup and fuzzy matching.
    Converts to lowercase, removes non-alphanumerics, handles common synonyms.
    """
    if not name:
        return ""

    norm = name.lower().strip()

    # Remove content inside parentheses if it's common translation (e.g. "aqua (water)" -> "aqua")
    # But preserve if it's part of the chemical name
    norm = re.sub(r'\((water|eau|aqua|plant|derived|certified organic|organic)\)', '', norm)

    # Remove punctuation
    norm = re.sub(r'[^\w\s]', '', norm)
    norm = re.sub(r'\s+', ' ', norm).strip()

    return norm

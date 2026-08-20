import json
import logging
from typing import List, Dict, Any, Optional
import httpx
from app.config import settings
from app.schemas.ingredient import IngredientDetail
from app.schemas.analysis import AnalysisResponse, CategorizedIngredients
from app.services.analysis_engine import AnalysisEngine

logger = logging.getLogger(__name__)


class AiService:
    @staticmethod
    async def analyze_product(
        product_name: str,
        ingredient_text: str,
        resolved_ingredients: List[IngredientDetail]
    ) -> AnalysisResponse:
        """
        Synthesizes an intelligent sensitive-skin cosmetic analysis using AI/NLP.
        Works seamlessly with configured external LLMs (Gemini / OpenAI) or built-in dermatological heuristic AI.
        """
        score, risk_category = AnalysisEngine.calculate_safety_score(resolved_ingredients)
        categories = AnalysisEngine.categorize_ingredients(resolved_ingredients)
        expert_summary, expert_recommendation = AnalysisEngine.generate_expert_summary(
            product_name, score, risk_category, categories
        )

        summary = expert_summary
        recommendation = expert_recommendation

        # If an external AI API key is configured, enrich summary and recommendation
        if settings.AI_API_KEY and settings.AI_PROVIDER.lower() == "gemini":
            try:
                ai_result = await AiService._call_gemini_api(product_name, ingredient_text, resolved_ingredients)
                if ai_result:
                    summary = ai_result.get("summary", summary)
                    recommendation = ai_result.get("recommendation", recommendation)
            except Exception as e:
                logger.warning(f"External Gemini AI call failed, using built-in dermatological engine: {e}")

        elif settings.OPENAI_API_KEY and settings.AI_PROVIDER.lower() == "openai":
            try:
                ai_result = await AiService._call_openai_api(product_name, ingredient_text, resolved_ingredients)
                if ai_result:
                    summary = ai_result.get("summary", summary)
                    recommendation = ai_result.get("recommendation", recommendation)
            except Exception as e:
                logger.warning(f"External OpenAI call failed, using built-in dermatological engine: {e}")

        return AnalysisResponse(
            product_name=product_name,
            safety_score=score,
            risk_category=risk_category,
            summary=summary,
            recommendation=recommendation,
            ingredients=resolved_ingredients,
            categories=categories
        )

    @staticmethod
    async def _call_gemini_api(
        product_name: str,
        ingredient_text: str,
        ingredients: List[IngredientDetail]
    ) -> Optional[Dict[str, Any]]:
        """Calls Google Gemini API for nuanced sensitive-skin cosmetic commentary."""
        url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={settings.AI_API_KEY}"
        
        prompt = (
            f"You are SkinSafe's cosmetic safety assistant. Analyze this cosmetic product for SENSITIVE SKIN.\n"
            f"Product Name: {product_name}\n"
            f"Ingredients: {ingredient_text}\n\n"
            f"IMPORTANT RULES:\n"
            f"1. Never claim to diagnose skin diseases, eczema, or medical conditions.\n"
            f"2. Use cautious language like 'may cause irritation in some individuals', 'generally considered well tolerated'.\n"
            f"3. Return ONLY a valid JSON object with keys 'summary' and 'recommendation'.\n"
        )

        payload = {
            "contents": [{"parts": [{"text": prompt}]}],
            "generationConfig": {"response_mime_type": "application/json"}
        }

        async with httpx.AsyncClient(timeout=10.0) as client:
            res = await client.post(url, json=payload)
            if res.status_code == 200:
                data = res.json()
                text_content = data["candidates"][0]["content"]["parts"][0]["text"]
                return json.loads(text_content)
        return None

    @staticmethod
    async def _call_openai_api(
        product_name: str,
        ingredient_text: str,
        ingredients: List[IngredientDetail]
    ) -> Optional[Dict[str, Any]]:
        """Calls OpenAI API for cosmetic analysis."""
        url = "https://api.openai.com/v1/chat/completions"
        headers = {
            "Authorization": f"Bearer {settings.OPENAI_API_KEY}",
            "Content-Type": "application/json"
        }
        prompt = (
            f"You are SkinSafe's cosmetic safety assistant. Analyze this cosmetic product for SENSITIVE SKIN.\n"
            f"Product Name: {product_name}\n"
            f"Ingredients: {ingredient_text}\n"
            f"Return JSON with keys 'summary' and 'recommendation'."
        )
        payload = {
            "model": "gpt-4o-mini",
            "messages": [{"role": "user", "content": prompt}],
            "response_format": {"type": "json_object"}
        }
        async with httpx.AsyncClient(timeout=10.0) as client:
            res = await client.post(url, headers=headers, json=payload)
            if res.status_code == 200:
                data = res.json()
                content = data["choices"][0]["message"]["content"]
                return json.loads(content)
        return None

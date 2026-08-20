import pytest


def test_analyze_safe_hydrating_cream(client, auth_headers):
    """Tests analyzing a safe soothing cream yields high score and LOW RISK."""
    payload = {
        "product_name": "Barrier Recovery Cream",
        "ingredient_text": "Water, Glycerin, Niacinamide, Panthenol, Ceramide NP, Squalane, Allantoin"
    }
    response = client.post("/api/analyze/text", json=payload, headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["product_name"] == "Barrier Recovery Cream"
    assert data["safety_score"] >= 80
    assert data["risk_category"] == "LOW RISK"
    assert len(data["ingredients"]) == 7
    assert len(data["categories"]["safe_ingredients"]) >= 4
    assert "disclaimer" in data
    assert "id" in data


def test_analyze_irritating_product(client, auth_headers):
    """Tests analyzing a formula with denatured alcohol, fragrance, and allergens applies penalties."""
    payload = {
        "product_name": "Scented Astringent Toner",
        "ingredient_text": "Water, Alcohol Denat., Fragrance, Limonene, Linalool, Sodium Lauryl Sulfate"
    }
    response = client.post("/api/analyze/text", json=payload, headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["safety_score"] < 60
    assert data["risk_category"] in ["HIGH RISK", "MODERATE RISK"]
    assert len(data["categories"]["fragrance_ingredients"]) >= 2
    assert len(data["categories"]["alcohol_ingredients"]) >= 1


def test_empty_ingredient_analysis_fails(client, auth_headers):
    """Tests submitting empty ingredient text returns HTTP 400."""
    payload = {
        "product_name": "Empty Product",
        "ingredient_text": ""
    }
    response = client.post("/api/analyze/text", json=payload, headers=auth_headers)
    assert response.status_code == 422 # Pydantic validation


def test_ingredient_lookup_endpoint(client):
    """Tests scientific ingredient lookup for Niacinamide."""
    response = client.get("/api/ingredients/Niacinamide")
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Niacinamide"
    assert data["risk_level"] == "LOW"
    assert "Vitamin B3" in data["description"]

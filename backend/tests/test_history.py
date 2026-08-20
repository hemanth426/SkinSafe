import pytest


def test_scan_history_workflow(client, auth_headers):
    """Tests scan creation, history listing, detail retrieval, and deletion."""
    # 1. Perform an analysis to create a history record
    payload = {
        "product_name": "Hydrating Gel Cleanser",
        "ingredient_text": "Water, Glycerin, Cocamidopropyl Betaine, Panthenol"
    }
    ana_resp = client.post("/api/analyze/text", json=payload, headers=auth_headers)
    assert ana_resp.status_code == 200
    analysis_id = ana_resp.json()["id"]

    # 2. Get history list
    hist_resp = client.get("/api/history", headers=auth_headers)
    assert hist_resp.status_code == 200
    history_items = hist_resp.json()
    assert len(history_items) >= 1
    assert any(h["id"] == analysis_id for h in history_items)

    # 3. Get history detail
    detail_resp = client.get(f"/api/history/{analysis_id}", headers=auth_headers)
    assert detail_resp.status_code == 200
    detail = detail_resp.json()
    assert detail["product_name"] == "Hydrating Gel Cleanser"
    assert "analysis_json" in detail

    # 4. Delete history item
    del_resp = client.delete(f"/api/history/{analysis_id}", headers=auth_headers)
    assert del_resp.status_code == 200

    # 5. Verify deleted
    verify_resp = client.get(f"/api/history/{analysis_id}", headers=auth_headers)
    assert verify_resp.status_code == 404

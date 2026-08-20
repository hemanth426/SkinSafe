import pytest


def test_saved_products_workflow(client, auth_headers):
    """Tests saving a product from analysis, listing saved items, and unsaving."""
    # 1. Create an analysis
    payload = {
        "product_name": "Calming Cica Serum",
        "ingredient_text": "Water, Centella Asiatica Extract, Madecassoside, Glycerin, Sodium Hyaluronate"
    }
    ana_resp = client.post("/api/analyze/text", json=payload, headers=auth_headers)
    assert ana_resp.status_code == 200
    analysis_id = ana_resp.json()["id"]

    # 2. Save product
    save_resp = client.post(
        "/api/saved",
        json={"analysis_id": analysis_id, "notes": "Holy grail calming serum!"},
        headers=auth_headers
    )
    assert save_resp.status_code == 201
    saved_data = save_resp.json()
    assert saved_data["product_name"] == "Calming Cica Serum"
    assert saved_data["notes"] == "Holy grail calming serum!"
    saved_id = saved_data["id"]

    # 3. List saved products
    list_resp = client.get("/api/saved", headers=auth_headers)
    assert list_resp.status_code == 200
    saved_items = list_resp.json()
    assert len(saved_items) >= 1
    assert any(s["analysis_id"] == analysis_id for s in saved_items)

    # 4. Remove saved product
    del_resp = client.delete(f"/api/saved/{saved_id}", headers=auth_headers)
    assert del_resp.status_code == 200

    # 5. Verify removed
    list_after = client.get("/api/saved", headers=auth_headers).json()
    assert not any(s["id"] == saved_id for s in list_after)

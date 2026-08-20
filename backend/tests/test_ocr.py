import io
from PIL import Image, ImageDraw


def test_ocr_image_upload_endpoint(client):
    """Tests uploading an image to the OCR extraction endpoint."""
    # Create a small valid test image in memory
    img = Image.new('RGB', (400, 150), color=(255, 255, 255))
    d = ImageDraw.Draw(img)
    d.text((10, 10), "Ingredients: Water, Glycerin, Niacinamide, Panthenol", fill=(0, 0, 0))

    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='PNG')
    img_byte_arr.seek(0)

    response = client.post(
        "/api/analyze/image",
        files={"file": ("label.png", img_byte_arr, "image/png")}
    )
    assert response.status_code == 200
    data = response.json()
    assert "extracted_text" in data
    assert "cleaned_ingredients" in data
    assert len(data["cleaned_ingredients"]) > 0


def test_ocr_invalid_file_type_fails(client):
    """Tests that uploading a non-image file returns 400 Bad Request."""
    file_bytes = io.BytesIO(b"this is not an image file")
    response = client.post(
        "/api/analyze/image",
        files={"file": ("test.txt", file_bytes, "text/plain")}
    )
    assert response.status_code == 400

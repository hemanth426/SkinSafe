import pytest


def test_health_check(client):
    """Tests the /api/health endpoint."""
    response = client.get("/api/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert "version" in data


def test_user_registration(client):
    """Tests new user registration."""
    payload = {
        "name": "Sarah Connor",
        "email": "sarah@example.com",
        "password": "StrongPassword123!",
        "skin_type": "Sensitive & Reactive"
    }
    response = client.post("/api/auth/register", json=payload)
    assert response.status_code == 201
    data = response.json()
    assert data["name"] == "Sarah Connor"
    assert data["email"] == "sarah@example.com"
    assert data["skin_type"] == "Sensitive & Reactive"
    assert "id" in data


def test_duplicate_registration_fails(client):
    """Tests that registering an already existing email returns HTTP 400."""
    payload = {
        "name": "Duplicate User",
        "email": "duplicate@example.com",
        "password": "Password123!"
    }
    resp1 = client.post("/api/auth/register", json=payload)
    assert resp1.status_code == 201

    resp2 = client.post("/api/auth/register", json=payload)
    assert resp2.status_code == 400
    assert "already exists" in resp2.json()["detail"]


def test_user_login_success(client):
    """Tests valid login returns JWT access token."""
    email = "login_success@example.com"
    pwd = "MySecretPassword123"
    client.post("/api/auth/register", json={"name": "Login Test", "email": email, "password": pwd})

    response = client.post("/api/auth/login", json={"email": email, "password": pwd})
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert data["token_type"] == "bearer"
    assert data["email"] == email


def test_user_login_invalid_password(client):
    """Tests invalid password returns HTTP 401."""
    email = "invalid_pwd@example.com"
    client.post("/api/auth/register", json={"name": "Invalid Pwd", "email": email, "password": "CorrectPassword123"})

    response = client.post("/api/auth/login", json={"email": email, "password": "WrongPassword"})
    assert response.status_code == 401
    assert "Invalid email or password" in response.json()["detail"]


def test_get_current_user_profile(client, auth_headers):
    """Tests fetching current user profile with JWT."""
    response = client.get("/api/users/me", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert "name" in data
    assert "email" in data
    assert "skin_type" in data


def test_update_preferences(client, auth_headers):
    """Tests updating skin type preference."""
    response = client.put("/api/users/preferences", json={"skin_type": "Acne-Prone & Sensitive"}, headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["skin_type"] == "Acne-Prone & Sensitive"

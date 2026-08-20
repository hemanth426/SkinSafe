import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.database import Base, get_db
from app.main import app
from app.services.ingredient_service import IngredientService

SQLALCHEMY_DATABASE_URL = "sqlite:///:memory:"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@pytest.fixture(scope="session", autouse=True)
def setup_test_database():
    """Initializes schema and seeds starter ingredients for all tests."""
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    try:
        IngredientService.seed_initial_ingredients(db)
    finally:
        db.close()
    yield
    Base.metadata.drop_all(bind=engine)


@pytest.fixture(scope="function")
def db_session():
    """Provides a transactional database session rolled back after each test."""
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()


@pytest.fixture(scope="function")
def client(db_session):
    """Provides a TestClient with database session override."""
    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


@pytest.fixture(scope="function")
def auth_headers(client):
    """Creates a registered test user and returns Authorization headers with JWT Bearer token."""
    user_data = {
        "name": "Sensitive Skin Tester",
        "email": f"tester_{pytest.test_counter if hasattr(pytest, 'test_counter') else 1}@example.com",
        "password": "Password123!",
        "skin_type": "Very Sensitive"
    }
    if not hasattr(pytest, 'test_counter'):
        pytest.test_counter = 1
    pytest.test_counter += 1

    # Register
    reg_resp = client.post("/api/auth/register", json=user_data)
    
    # Login
    login_resp = client.post("/api/auth/login", json={
        "email": user_data["email"],
        "password": user_data["password"]
    })
    assert login_resp.status_code == 200
    token = login_resp.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}

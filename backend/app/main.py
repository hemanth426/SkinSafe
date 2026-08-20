import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.database import init_db, SessionLocal
from app.services.ingredient_service import IngredientService
from app.routers import auth, users, analyze, history, saved, ingredients, health

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup and shutdown events."""
    logger.info("Starting up SkinSafe Backend...")
    # Initialize database tables
    init_db()
    # Seed starter ingredients
    db = SessionLocal()
    try:
        IngredientService.seed_initial_ingredients(db)
    finally:
        db.close()
    yield
    logger.info("Shutting down SkinSafe Backend...")


app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="SkinSafe API – AI-Powered Cosmetic Ingredient Analyzer for Sensitive Skin",
    lifespan=lifespan
)

# Configure CORS
origins = settings.CORS_ORIGINS if isinstance(settings.CORS_ORIGINS, list) else ["*"]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include API Routers under /api
app.include_router(health.router, prefix=settings.API_V1_STR)
app.include_router(auth.router, prefix=settings.API_V1_STR)
app.include_router(users.router, prefix=settings.API_V1_STR)
app.include_router(analyze.router, prefix=settings.API_V1_STR)
app.include_router(history.router, prefix=settings.API_V1_STR)
app.include_router(saved.router, prefix=settings.API_V1_STR)
app.include_router(ingredients.router, prefix=settings.API_V1_STR)


@app.get("/")
def root():
    return {
        "message": "Welcome to SkinSafe API – AI-Powered Cosmetic Ingredient Analyzer for Sensitive Skin",
        "docs": "/docs",
        "version": settings.VERSION,
        "health": f"{settings.API_V1_STR}/health"
    }

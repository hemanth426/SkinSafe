# SkinSafe – AI-Powered Cosmetic Ingredient Analyzer for Sensitive Skin

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20FastAPI-2E7D6F.svg)](https://github.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-teal.svg)](https://developer.android.com/jetpack/compose)
[![Python](https://img.shields.io/badge/Python-3.11-blue.svg)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.110.0-009688.svg)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg)](https://www.postgresql.org/)

SkinSafe is an end-to-end, production-ready mobile application and AI backend engineered to safeguard sensitive and reactive skin from irritating, allergenic, comedogenic, or barrier-stripping cosmetic chemicals.

Users can snap a photo of any cosmetic ingredient list (INCI deck), upload packaging photos from their gallery, or paste ingredient texts manually. SkinSafe cleans the OCR text, normalizes INCI chemical names, checks an extensive dermatological database, and synthesizes an explainable **Skin Safety Score (0–100)** with clear sensitive-skin recommendations.

---

## Architecture Overview

```
                      +------------------------------------------+
                      |         Android Client Application       |
                      |   (Kotlin + Jetpack Compose + CameraX)   |
                      +--------------------+---------------------+
                                           |  HTTPS / REST (Retrofit)
                                           v
                      +------------------------------------------+
                      |           FastAPI REST Backend           |
                      |          (Python 3.11 + Uvicorn)         |
                      +----+-------------------+---------------+--+
                           |                   |               |
                           v                   v               v
            +-------------------+    +------------------+  +------------------+
            |    PostgreSQL     |    |   OCR Engine     |  | AI / Heuristic   |
            |     Database      |    | (OpenCV + PIL +  |  | Dermatological   |
            | (Users, Products, |    |   Tesseract)     |  |  Safety Engine   |
            | Analyses, Library)|    |                  |  | (Offline/Gemini) |
            +-------------------+    +------------------+  +------------------+
```

---

## Features

- **Real User Authentication**: JWT Bearer token authentication with secure password hashing (`bcrypt`) and persistent local Android token management.
- **CameraX Live Scanner**: Live viewfinder with high-contrast targeting overlay, flash toggle, and instant image capture.
- **Advanced Image Preprocessing & OCR**: Bilateral denoising, adaptive thresholding, and contrast normalization using OpenCV and Pillow to cleanly extract INCI ingredient lists from cylindrical bottles and glossy boxes.
- **Editable OCR Review Screen**: Users can review, add, or correct any OCR misreads prior to triggering AI analysis.
- **Scientific Dermatological Scoring Engine**:
  - Starts at 100 with weighted deductions for allergens, drying alcohols, harsh surfactants, and pore-clogging comedogenic lipids.
  - Multi-category sorting: *Safe & Conditioning*, *Caution Required*, *Potential Irritants*, *Potential Allergens*, *Fragrance Ingredients*, *Drying Alcohols*, and *Comedogenic Pore-Cloggers*.
- **Configurable AI Engine**: Works 100% offline out-of-the-box using the built-in dermatological heuristic AI, or connects to Google Gemini / OpenAI when an API key is provided.
- **15 Complete Jetpack Compose Screens**:
  1. Splash Screen
  2. Onboarding Carousel Screen
  3. Login Screen
  4. Registration Screen
  5. Home Dashboard
  6. Scanner Screen (CameraX + Gallery)
  7. Manual Input Screen
  8. OCR Review Screen
  9. Loading Analysis Screen (Animated multi-stage progress)
  10. Analysis Result Screen (Circular score ring, category breakdown, recommendations)
  11. Ingredient Detail Screen (Deep scientific profile)
  12. Scan History Screen
  13. Saved Products Screen
  14. Profile & Skin Type Preference Screen
  15. Settings Screen (Dynamic Backend URL Switcher, Terms & Medical Disclaimer)
- **Zero-Hardcoded Local Network Switching**: Includes dynamic runtime settings in the Android app allowing instant switching between Android Emulator (`10.0.2.2:8000`), Physical Device LAN IP (`http://192.168.x.x:8000`), and Cloud URLs.

---

## Project Structure

```
SkinSafe/
├── android/
│   ├── app/
│   │   ├── build.gradle.kts
│   │   ├── proguard-rules.pro
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml
│   │       │   ├── java/com/skinsafe/app/
│   │       │   │   ├── SkinSafeApp.kt
│   │       │   │   ├── MainActivity.kt
│   │       │   │   ├── data/
│   │       │   │   │   ├── api/ (ApiService, ApiClient, AuthInterceptor, NetworkResult)
│   │       │   │   │   ├── local/ (TokenManager, SettingsPreferences)
│   │       │   │   │   ├── models/ (AuthModels, AnalysisModels, IngredientModels, HistoryModels)
│   │       │   │   │   └── repository/ (AuthRepository, AnalysisRepository, HistoryRepository, SavedProductsRepository)
│   │       │   │   ├── ui/
│   │       │   │   │   ├── navigation/ (Screen, NavGraph)
│   │       │   │   │   ├── theme/ (Color, Type, Theme)
│   │       │   │   │   ├── components/ (CircularScoreIndicator, RiskBadge, IngredientCard, SkinSafeTopBar, CommonButtons, ErrorBanner)
│   │       │   │   │   ├── screens/ (15 Production Jetpack Compose Screens)
│   │       │   │   │   └── viewmodels/ (8 MVVM ViewModels)
│   │       │   └── res/
│   │       └── test/ (JUnit & MockK Unit Tests)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradlew & gradlew.bat
│   └── gradle/libs.versions.toml
├── backend/
│   ├── app/
│   │   ├── main.py (FastAPI Application Entrypoint)
│   │   ├── config.py (Pydantic BaseSettings)
│   │   ├── database.py (SQLAlchemy Engine & Session)
│   │   ├── models/ (User, Product, Ingredient, Analysis, SavedProduct)
│   │   ├── schemas/ (Pydantic Request & Response Schemas)
│   │   ├── routers/ (auth, users, analyze, history, saved, ingredients, health)
│   │   ├── services/ (ingredient_service, analysis_engine, ocr_service, ai_service, auth_service)
│   │   ├── data/ (seed_ingredients.py - 70+ INCI Database)
│   │   └── utils/ (security.py, text_cleaner.py)
│   ├── tests/ (Pytest test suite: test_auth, test_analysis, test_ocr, test_history, test_saved)
│   ├── requirements.txt
│   ├── .env.example
│   └── Dockerfile
├── database/
│   └── schema.sql (PostgreSQL DDL)
├── docker-compose.yml
├── build_apk.sh & build_apk.bat
└── README.md
```

---

## Quick Start Guide

### Option 1: Run with Docker Compose (Recommended)

To launch the backend and PostgreSQL database with a single command:

```bash
docker compose up --build
```

The FastAPI backend will start at: `http://localhost:8000`
- Swagger UI Documentation: `http://localhost:8000/docs`
- Health Check: `http://localhost:8000/api/health`

---

### Option 2: Run Backend Locally with Python

1. **Navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Create and activate a virtual environment:**
   ```bash
   python -m venv venv
   # On Windows:
   .\venv\Scripts\activate
   # On macOS/Linux:
   source venv/bin/activate
   ```

3. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure Environment Variables:**
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   *(Note: SQLite is configured by default for zero-setup local development!)*

5. **Start the FastAPI server:**
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```

---

## Running Backend Tests

Run the full automated test suite with pytest:

```bash
cd backend
pytest -v
```

Tests cover:
- Health check verification
- User registration and duplicate rejection
- JWT login and password hash validation
- Text cosmetic ingredient analysis & risk scoring
- OCR image upload and text extraction
- Scan history persistence and deletion
- Saved products bookmarking and removal

---

## Android App Setup & Running

1. **Open Android Studio:**
   - Select **Open** and choose the `SkinSafe/android` folder.
   - Allow Gradle to sync dependencies.

2. **Connecting to the Backend:**
   - **Android Emulator**: The app is pre-configured to connect to `http://10.0.2.2:8000/` automatically.
   - **Physical Android Device**:
     1. Ensure your phone and computer are on the same Wi-Fi network.
     2. Find your computer's local IP address (e.g. `192.168.1.50`).
     3. Open **SkinSafe** on your phone, navigate to **Settings** (gear icon on Login or Home screen), and enter `http://192.168.1.50:8000/`. Tap **Save Server Configuration**.

3. **Run on Device or Emulator:**
   - Click the green **Run (▶)** button in Android Studio.

---

## APK Generation

### Using Build Scripts:

- **Windows**:
  ```cmd
  build_apk.bat
  ```
- **macOS / Linux**:
  ```bash
  chmod +x build_apk.sh
  ./build_apk.sh
  ```

### Manual Gradle Commands:

To generate the debug APK:
```bash
cd android
./gradlew assembleDebug
```

**APK Output Location:**
```
android/app/build/outputs/apk/debug/app-debug.apk
```

To install directly onto a connected phone or emulator:
```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

---

## REST API Reference

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/health` | Service health status check | No |
| `POST` | `/api/auth/register` | Register new user account | No |
| `POST` | `/api/auth/login` | Authenticate user & issue JWT | No |
| `GET` | `/api/users/me` | Fetch authenticated user profile | Yes (JWT) |
| `PUT` | `/api/users/preferences` | Update skin sensitivity preference | Yes (JWT) |
| `POST` | `/api/analyze/text` | Analyze cosmetic ingredient text deck | Yes (JWT) |
| `POST` | `/api/analyze/image` | Upload image for OCR extraction | Optional |
| `GET` | `/api/history` | List user's past cosmetic scans | Yes (JWT) |
| `GET` | `/api/history/{id}` | Retrieve specific scan details | Yes (JWT) |
| `DELETE` | `/api/history/{id}` | Delete specific scan record | Yes (JWT) |
| `POST` | `/api/saved` | Bookmark/save product analysis | Yes (JWT) |
| `GET` | `/api/saved` | List bookmarked products | Yes (JWT) |
| `DELETE` | `/api/saved/{id}` | Remove bookmarked product | Yes (JWT) |
| `GET` | `/api/ingredients/{name}` | Look up scientific ingredient details | No |

---

## Medical Disclaimer

> [!CAUTION]
> **SkinSafe is for informational and educational purposes only.** It does not provide medical diagnoses, treatment plans, or clinical guarantees. Individual biological allergic sensitivities vary. Always consult a board-certified dermatologist for medical dermatological conditions and perform a 24-hour patch test before introducing new skincare formulations.

---

## License

SkinSafe is open-source software built for educational and cosmetic safety research.

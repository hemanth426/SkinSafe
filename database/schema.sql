-- SkinSafe Database Schema for PostgreSQL

-- Create extension for UUID generation if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    skin_type VARCHAR(100) DEFAULT 'Sensitive',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 2. Products Table
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- 3. Ingredients Table
CREATE TABLE IF NOT EXISTS ingredients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    normalized_name VARCHAR(255) NOT NULL,
    purpose VARCHAR(255),
    risk_level VARCHAR(50) NOT NULL DEFAULT 'LOW', -- SAFE, LOW, MODERATE, HIGH
    description TEXT,
    irritation_potential VARCHAR(50) DEFAULT 'Low', -- None, Low, Medium, High
    allergy_potential VARCHAR(50) DEFAULT 'Low',    -- None, Low, Medium, High
    comedogenic_rating INT DEFAULT 0,               -- 0 to 5
    is_fragrance BOOLEAN DEFAULT FALSE,
    is_alcohol BOOLEAN DEFAULT FALSE,
    sensitive_concern TEXT,
    recommendation TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ingredients_normalized ON ingredients(normalized_name);
CREATE INDEX IF NOT EXISTS idx_ingredients_risk ON ingredients(risk_level);

-- 4. Analyses Table
CREATE TABLE IF NOT EXISTS analyses (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id INT REFERENCES products(id) ON DELETE SET NULL,
    product_name VARCHAR(255) NOT NULL,
    ingredient_text TEXT NOT NULL,
    safety_score INT NOT NULL, -- 0 to 100
    risk_category VARCHAR(50) NOT NULL, -- LOW RISK, MODERATE RISK, HIGH RISK
    summary TEXT,
    recommendation TEXT,
    analysis_json JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analyses_user ON analyses(user_id);
CREATE INDEX IF NOT EXISTS idx_analyses_created_at ON analyses(created_at DESC);

-- 5. Saved Products Table
CREATE TABLE IF NOT EXISTS saved_products (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    analysis_id INT NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_analysis UNIQUE (user_id, analysis_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_user ON saved_products(user_id);

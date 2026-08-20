from sqlalchemy import Column, Integer, String, Text, Boolean, DateTime
from sqlalchemy.sql import func
from app.database import Base


class Ingredient(Base):
    __tablename__ = "ingredients"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), unique=True, index=True, nullable=False)
    normalized_name = Column(String(255), index=True, nullable=False)
    purpose = Column(String(255), nullable=True)
    risk_level = Column(String(50), default="LOW", index=True, nullable=False) # SAFE, LOW, MODERATE, HIGH
    description = Column(Text, nullable=True)
    irritation_potential = Column(String(50), default="Low") # None, Low, Medium, High
    allergy_potential = Column(String(50), default="Low")    # None, Low, Medium, High
    comedogenic_rating = Column(Integer, default=0)          # 0 to 5
    is_fragrance = Column(Boolean, default=False)
    is_alcohol = Column(Boolean, default=False)
    sensitive_concern = Column(Text, nullable=True)
    recommendation = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

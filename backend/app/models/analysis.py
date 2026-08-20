from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, JSON
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class Analysis(Base):
    __tablename__ = "analyses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    product_id = Column(Integer, ForeignKey("products.id", ondelete="SET NULL"), nullable=True)
    product_name = Column(String(255), nullable=False)
    ingredient_text = Column(Text, nullable=False)
    safety_score = Column(Integer, nullable=False) # 0 - 100
    risk_category = Column(String(50), nullable=False) # LOW RISK, MODERATE RISK, HIGH RISK
    summary = Column(Text, nullable=True)
    recommendation = Column(Text, nullable=True)
    analysis_json = Column(JSON, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), index=True)

    user = relationship("User", back_populates="analyses")
    product = relationship("Product", back_populates="analyses")
    saved_products = relationship("SavedProduct", back_populates="analysis", cascade="all, delete-orphan")

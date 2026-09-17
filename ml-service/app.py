"""
FastAPI Microservice for Smart Campus Machine Learning Integration.

Exposes REST endpoints:
- GET  /health           : Health check
- GET  /model-info       : Evaluation metrics, feature schema, active algorithm
- POST /predict          : Predict single student-event registration probability
- POST /predict-batch    : Batch predictions for multiple events
- POST /predict-sentiment: ML sentiment prediction for feedback text
"""

import os
import sys
from typing import List, Optional
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

# Ensure relative path resolution works whether run from root or ml-service/
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

model_file = os.path.join(current_dir, "models", "event_registration_model.pkl")
sentiment_file = os.path.join(current_dir, "models", "sentiment_model.pkl")

from predict import EventRegistrationPredictor, FeedbackSentimentPredictor

app = FastAPI(
    title="CampuSphere - Machine Learning Service",
    description="Machine learning prediction microservice for Event Registration Probability and Student Feedback Sentiment Analysis.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize Predictors
registration_predictor = EventRegistrationPredictor(model_path=model_file)
try:
    sentiment_predictor = FeedbackSentimentPredictor(model_path=sentiment_file)
except Exception as e:
    sentiment_predictor = None
    print(f"Warning: Sentiment model could not be initialized: {e}")


# --- Pydantic Schemas ---

class PredictionRequest(BaseModel):
    student_department: str = Field(..., example="CSE", description="Department of the student")
    student_year: int = Field(..., example=4, description="Student year of study (1-4)")
    event_category: str = Field(..., example="Technical", description="Category of the event")
    event_department: str = Field(..., example="CSE", description="Department hosting the event or 'All'")
    previous_registrations: int = Field(0, example=5, description="Number of past registrations")
    previous_attendance: int = Field(0, example=4, description="Number of past attended events")
    category_match: int = Field(..., example=1, description="1 if event matches student interest, 0 otherwise")
    department_match: int = Field(..., example=1, description="1 if event department matches student department, 0 otherwise")
    event_popularity: int = Field(50, example=75, description="Popularity index or registration percentage (0-100)")
    event_id: Optional[int] = Field(None, description="Optional Event ID for tracking in batch requests")


class PredictionResponse(BaseModel):
    prediction: int = Field(..., description="1 = Likely to Register, 0 = Unlikely")
    probability: float = Field(..., description="Model-predicted probability between 0.0 and 1.0")
    model_used: str = Field(..., description="Name of the trained scikit-learn model")
    event_id: Optional[int] = Field(None, description="Optional Event ID matching request")


class BatchPredictionRequest(BaseModel):
    items: List[PredictionRequest]


class BatchPredictionResponse(BaseModel):
    predictions: List[PredictionResponse]


class SentimentRequest(BaseModel):
    text: str = Field(..., example="The event was well structured and highly educational!", description="Student feedback comment")


class SentimentResponse(BaseModel):
    sentiment: str = Field(..., example="POSITIVE", description="POSITIVE, NEGATIVE, or NEUTRAL")
    score: float = Field(..., example=0.94, description="Confidence score")
    model_used: str = Field("TF-IDF + Logistic Regression")


# --- Endpoints ---

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "CampuSphere ML Microservice",
        "model_loaded": registration_predictor is not None,
        "sentiment_model_loaded": sentiment_predictor is not None,
        "active_algorithm": registration_predictor.model_name
    }


@app.get("/model-info")
def model_info():
    """
    Returns training metadata, feature schema, and comparative evaluation metrics
    for Logistic Regression, Random Forest, and Decision Tree.
    """
    return registration_predictor.metadata


@app.post("/predict", response_model=PredictionResponse)
def predict_registration(req: PredictionRequest):
    """
    Predicts whether a given student is likely to register for a specific campus event.
    Returns binary prediction (1/0) and estimated probability.
    """
    try:
        data = req.model_dump()
        result = registration_predictor.predict_one(data)
        return PredictionResponse(
            prediction=result["prediction"],
            probability=result["probability"],
            model_used=result["model_used"],
            event_id=req.event_id
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {str(e)}")


@app.post("/predict-batch", response_model=BatchPredictionResponse)
def predict_registration_batch(req: BatchPredictionRequest):
    """
    Predicts registration probability for multiple events in a single HTTP round-trip.
    Used by Spring Boot RecommendationService for efficient ranking.
    """
    try:
        items_data = [item.model_dump() for item in req.items]
        results = registration_predictor.predict_batch(items_data)
        responses = []
        for i, res in enumerate(results):
            responses.append(PredictionResponse(
                prediction=res["prediction"],
                probability=res["probability"],
                model_used=res["model_used"],
                event_id=req.items[i].event_id
            ))
        return BatchPredictionResponse(predictions=responses)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Batch prediction error: {str(e)}")


@app.post("/predict-sentiment", response_model=SentimentResponse)
def predict_sentiment(req: SentimentRequest):
    """
    Predicts sentiment for student feedback using TF-IDF + Logistic Regression.
    """
    if not sentiment_predictor:
        raise HTTPException(status_code=503, detail="Sentiment model is not available.")
    try:
        res = sentiment_predictor.predict(req.text)
        return SentimentResponse(
            sentiment=res["sentiment"],
            score=res["score"],
            model_used=res["model_used"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Sentiment prediction error: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)

"""
Model Inference and Prediction Module.

Provides:
- EventRegistrationPredictor: Loads trained scikit-learn pipeline and predicts registration probability
- FeedbackSentimentPredictor: Loads trained TF-IDF + Logistic Regression sentiment model
"""

import os
import joblib
import pandas as pd
from typing import Dict, Any, List

current_dir = os.path.dirname(os.path.abspath(__file__))
DEFAULT_MODEL_PATH = os.path.join(current_dir, "models", "event_registration_model.pkl")
DEFAULT_SENTIMENT_PATH = os.path.join(current_dir, "models", "sentiment_model.pkl")

class EventRegistrationPredictor:
    def __init__(self, model_path: str = DEFAULT_MODEL_PATH):
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"Model file not found at: {model_path}. Please run train_model.py first.")
        
        bundle = joblib.load(model_path)
        self.pipeline = bundle["pipeline"]
        self.metadata = bundle["metadata"]
        self.model_name = self.metadata.get("best_model_name", "Logistic Regression")
        self.all_features = self.metadata.get("all_features", [
            "student_year", "previous_registrations", "previous_attendance",
            "category_match", "department_match", "event_popularity",
            "student_department", "event_category", "event_department"
        ])

    def predict_one(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Predict registration for a single student-event pair.
        """
        df = pd.DataFrame([data])
        # Ensure all required features are present
        for col in self.all_features:
            if col not in df.columns:
                df[col] = 0 if col in self.metadata.get("numeric_features", []) else "Unknown"

        df = df[self.all_features]
        pred = int(self.pipeline.predict(df)[0])
        proba = float(self.pipeline.predict_proba(df)[0][1])

        return {
            "prediction": pred,
            "probability": round(proba, 4),
            "model_used": self.model_name
        }

    def predict_batch(self, items: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Predict registration for multiple events at once (efficient batch processing).
        """
        if not items:
            return []

        df = pd.DataFrame(items)
        for col in self.all_features:
            if col not in df.columns:
                df[col] = 0 if col in self.metadata.get("numeric_features", []) else "Unknown"

        df = df[self.all_features]
        preds = self.pipeline.predict(df)
        probas = self.pipeline.predict_proba(df)[:, 1]

        results = []
        for i in range(len(items)):
            results.append({
                "prediction": int(preds[i]),
                "probability": round(float(probas[i]), 4),
                "model_used": self.model_name
            })
        return results


class FeedbackSentimentPredictor:
    def __init__(self, model_path: str = DEFAULT_SENTIMENT_PATH):
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"Sentiment model not found at: {model_path}")
        self.pipeline = joblib.load(model_path)

    def predict(self, comment: str) -> Dict[str, Any]:
        if not comment or not comment.strip():
            return {
                "sentiment": "NEUTRAL",
                "score": 0.5,
                "model_used": "TF-IDF + Logistic Regression"
            }
        
        pred = self.pipeline.predict([comment])[0]
        # Get probability of the predicted class
        probas = self.pipeline.predict_proba([comment])[0]
        classes = list(self.pipeline.classes_)
        score = float(probas[classes.index(pred)])

        return {
            "sentiment": str(pred),
            "score": round(score, 4),
            "model_used": "TF-IDF + Logistic Regression"
        }

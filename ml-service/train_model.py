"""
Complete Machine Learning Training and Evaluation Pipeline.

This script:
1. Loads historical/synthetic interaction training data.
2. Performs data cleaning and feature engineering.
3. Sets up a scikit-learn preprocessing pipeline (OneHotEncoder + StandardScaler).
4. Splits into stratified train and test sets (80% train / 20% test).
5. Trains and evaluates candidate models:
   - Logistic Regression (recommended baseline)
   - Random Forest Classifier
   - Decision Tree Classifier
6. Evaluates each model using:
   - Accuracy
   - Precision
   - Recall
   - F1-Score
   - Confusion Matrix
   - ROC-AUC
7. Selects the best performing model based on test F1-score and saves the full pipeline using joblib.
8. Trains an auxiliary ML Sentiment Classifier (TF-IDF + Logistic Regression) for student feedback.
9. Writes a comprehensive evaluation report in JSON and human-readable text formats.
"""

import os
import json
import joblib
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    roc_auc_score,
    confusion_matrix,
    classification_report
)

def train_event_registration_models(data_path: str = "ml-service/data/training_data.csv"):
    print(f"Loading event interaction dataset from {data_path}...")
    df = pd.read_csv(data_path)
    
    # Feature columns
    numeric_features = [
        "student_year",
        "previous_registrations",
        "previous_attendance",
        "category_match",
        "department_match",
        "event_popularity"
    ]
    
    categorical_features = [
        "student_department",
        "event_category",
        "event_department"
    ]
    
    target_column = "registered"
    
    X = df[numeric_features + categorical_features]
    y = df[target_column]
    
    # Preprocessor using ColumnTransformer
    preprocessor = ColumnTransformer(
        transformers=[
            ("num", StandardScaler(), numeric_features),
            ("cat", OneHotEncoder(handle_unknown="ignore", sparse_output=False), categorical_features)
        ]
    )
    
    # Stratified Train/Test Split (80% Train, 20% Test)
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, stratify=y
    )
    
    print(f"Training samples: {len(X_train)}, Testing samples: {len(X_test)}")
    
    # Candidate models to evaluate
    candidate_models = {
        "Logistic Regression": LogisticRegression(max_iter=1000, random_state=42, C=1.0),
        "Random Forest": RandomForestClassifier(n_estimators=100, max_depth=8, random_state=42),
        "Decision Tree": DecisionTreeClassifier(max_depth=6, random_state=42)
    }
    
    evaluation_results = {}
    fitted_pipelines = {}
    
    for name, clf in candidate_models.items():
        pipeline = Pipeline(steps=[
            ("preprocessor", preprocessor),
            ("classifier", clf)
        ])
        
        pipeline.fit(X_train, y_train)
        fitted_pipelines[name] = pipeline
        
        # Predictions on test set
        y_pred = pipeline.predict(X_test)
        y_prob = pipeline.predict_proba(X_test)[:, 1]
        
        acc = accuracy_score(y_test, y_pred)
        prec = precision_score(y_test, y_pred, zero_division=0)
        rec = recall_score(y_test, y_pred, zero_division=0)
        f1 = f1_score(y_test, y_pred, zero_division=0)
        roc_auc = roc_auc_score(y_test, y_prob)
        cm = confusion_matrix(y_test, y_pred).tolist()
        
        evaluation_results[name] = {
            "accuracy": round(float(acc), 4),
            "precision": round(float(prec), 4),
            "recall": round(float(rec), 4),
            "f1_score": round(float(f1), 4),
            "roc_auc": round(float(roc_auc), 4),
            "confusion_matrix": cm,
            "classification_report": classification_report(y_test, y_pred, output_dict=True)
        }
        
        print(f"\n--- {name} ---")
        print(f"Accuracy:  {acc:.4f}")
        print(f"Precision: {prec:.4f}")
        print(f"Recall:    {rec:.4f}")
        print(f"F1 Score:  {f1:.4f}")
        print(f"ROC-AUC:   {roc_auc:.4f}")
        print(f"Confusion Matrix:\n{confusion_matrix(y_test, y_pred)}")

    # Select best model based on F1-score (or ROC-AUC)
    best_model_name = max(evaluation_results, key=lambda k: evaluation_results[k]["f1_score"])
    best_pipeline = fitted_pipelines[best_model_name]
    print(f"\nSelected Best Model: {best_model_name} (F1 Score: {evaluation_results[best_model_name]['f1_score']})")

    # Save model artifacts
    os.makedirs("ml-service/models", exist_ok=True)
    model_artifact_path = "ml-service/models/event_registration_model.pkl"
    
    metadata = {
        "best_model_name": best_model_name,
        "numeric_features": numeric_features,
        "categorical_features": categorical_features,
        "all_features": numeric_features + categorical_features,
        "target": target_column,
        "evaluation_results": evaluation_results,
        "train_size": len(X_train),
        "test_size": len(X_test),
        "note": "Synthetic interaction data is used for initial model training because the application does not yet have sufficient historical production data."
    }
    
    # Save best pipeline bundle
    save_bundle = {
        "pipeline": best_pipeline,
        "metadata": metadata
    }
    joblib.dump(save_bundle, model_artifact_path)
    print(f"Saved best model pipeline to {model_artifact_path}")
    
    # Save evaluation report JSON
    report_json_path = "ml-service/models/evaluation_report.json"
    with open(report_json_path, "w") as f:
        json.dump(metadata, f, indent=2)
    print(f"Saved evaluation report JSON to {report_json_path}")
    
    # Save human-readable summary TXT
    report_txt_path = "ml-service/models/evaluation_report.txt"
    with open(report_txt_path, "w") as f:
        f.write("=" * 60 + "\n")
        f.write("SMART CAMPUS EVENT REGISTRATION PREDICTION MODEL EVALUATION\n")
        f.write("=" * 60 + "\n\n")
        f.write(f"Training Samples: {len(X_train)}\n")
        f.write(f"Testing Samples:  {len(X_test)}\n")
        f.write(f"Features:         {', '.join(numeric_features + categorical_features)}\n")
        f.write(f"Target:           {target_column} (1 = Likely to Register, 0 = Unlikely)\n\n")
        f.write(f"DISCLOSURE: {metadata['note']}\n\n")
        f.write("-" * 60 + "\n")
        f.write(f"{'Model':<22} | {'Accuracy':<8} | {'Precision':<9} | {'Recall':<8} | {'F1':<8} | {'ROC-AUC':<8}\n")
        f.write("-" * 60 + "\n")
        for m_name, m_res in evaluation_results.items():
            f.write(f"{m_name:<22} | {m_res['accuracy']:<8.4f} | {m_res['precision']:<9.4f} | {m_res['recall']:<8.4f} | {m_res['f1_score']:<8.4f} | {m_res['roc_auc']:<8.4f}\n")
        f.write("-" * 60 + "\n\n")
        f.write(f"SELECTED PRODUCTION MODEL: {best_model_name}\n")
    print(f"Saved evaluation summary text to {report_txt_path}")
    
    return best_model_name, metadata


def train_sentiment_model(sentiment_data_path: str = "ml-service/data/feedback_sentiment_data.csv"):
    """
    Train an auxiliary ML sentiment classifier (TF-IDF + Logistic Regression).
    Pipeline: Student Feedback -> Text Cleaning -> TF-IDF -> ML Classifier -> Positive / Neutral / Negative.
    """
    print(f"\nTraining auxiliary ML sentiment model from {sentiment_data_path}...")
    df = pd.read_csv(sentiment_data_path)
    
    X = df["comment"]
    y = df["sentiment"]
    
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.20, random_state=42, stratify=y)
    
    sentiment_pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1, stop_words="english")),
        ("clf", LogisticRegression(max_iter=1000, random_state=42))
    ])
    
    sentiment_pipeline.fit(X_train, y_train)
    
    y_pred = sentiment_pipeline.predict(X_test)
    acc = accuracy_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred, average="weighted")
    print(f"ML Sentiment Model Test Accuracy: {acc:.4f}, Weighted F1: {f1:.4f}")
    
    sentiment_model_path = "ml-service/models/sentiment_model.pkl"
    joblib.dump(sentiment_pipeline, sentiment_model_path)
    print(f"Saved sentiment model to {sentiment_model_path}")


if __name__ == "__main__":
    train_event_registration_models()
    train_sentiment_model()

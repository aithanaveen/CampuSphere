# CampuSphere — Smart Campus Event Management System

A comprehensive **Full-Stack Java + Machine Learning + Generative AI** enterprise web application for higher education institutions.

The system combines **supervised machine learning** (scikit-learn binary classification predicting event registration probabilities) and **Generative AI** (OpenAI GPT-4o-mini generating contextual recommendation explanations and summarizing feedback), built on an enterprise Spring Boot 3.3.4, MySQL, Spring Security, and Thymeleaf foundation.

---

## 📌 Project Overview

**CampuSphere** enables campus administrators to organize and monitor events while empowering students to discover personalized event recommendations, complete digital registrations, track attendance, and submit sentiment-analyzed feedback.

### Key Differentiator: Genuine ML + Generative AI Synergy
- **Machine Learning (Predictive AI)**: A scikit-learn classification pipeline hosted on a dedicated Python FastAPI microservice computes personalized **event registration probabilities** for students based on profile and historical interaction features.
- **Generative AI (Explanatory AI)**: OpenAI's GPT-4o-mini generates natural-language justifications explaining *why* the ML model recommended each event, synthesizes qualitative attendee feedback into executive summaries, and classifies feedback sentiment.
- **Deterministic & Local Fallbacks**: If either the ML microservice or OpenAI API is unavailable, the application gracefully falls back to deterministic heuristic algorithms so the user experience is never interrupted.

---

## 🧠 AI & Machine Learning Architecture

```
                                  SMART CAMPUS SYSTEM
                                           │
                    ┌──────────────────────┴──────────────────────┐
                    │                                             │
             Spring Boot 3.3.4                                FastAPI
               (Port 8080)                                  (Port 8000)
                    │                                             │
      ┌─────────────┼─────────────┐                 ┌─────────────┴─────────────┐
      │             │             │                 │                           │
    MySQL       OpenAI API    RestClient ──────► POST /predict             scikit-learn
   Database    (GPT-4o-mini) (HTTP Client)       POST /predict-batch          Pipeline
      │             │                               │                           │
      │       • Explanations                        │                 • Logistic Regression
      │       • Summaries                           │                 • Random Forest
      │       • Sentiment                           │                 • ColumnTransformer
      │             │                               │                           │
      └─────────────┴──────────────┬────────────────┴───────────────────────────┘
                                   │
                      Thymeleaf + Bootstrap UI
                  (Student & Admin Web Dashboards)
```

### Communication Flow:
1. **Spring Boot $\rightarrow$ MySQL**: Relational persistence for students, events, registrations, attendance, and feedback.
2. **Spring Boot $\rightarrow$ FastAPI $\rightarrow$ ML Model**: `MLRecommendationService` sends student and event interaction feature vectors via `RestClient` to obtain ML-predicted probabilities.
3. **Spring Boot $\rightarrow$ OpenAI API**: `RecommendationAI`, `FeedbackSummarizer`, and `SentimentAnalyzer` send prompt contexts to generate human-readable explanations and summaries.

---

## 🤖 Machine Learning Module Details

### ML Objective
Given a student profile and an event's details, predict whether the student is likely to register for the event.
- **Problem Type**: Supervised Binary Classification
- **Target Label**: `registered` (`1` = Likely to register, `0` = Unlikely to register)
- **Model Output**: Predicted Class (`0`/`1`) and Model Probability ($0.0 \le p \le 1.0$)

### Feature Engineering
Features are derived from student records, event specifications, and historical campus activity:

| Feature Name | Type | Description |
|--------------|------|-------------|
| `student_department` | Categorical | Department of the student (e.g. CSE, ECE, IT, MECH, CIVIL) |
| `student_year` | Numerical | Year of study (1 to 4) |
| `event_category` | Categorical | Event category (Technical, Workshop, Cultural, Seminar, Hackathon, Sports) |
| `event_department` | Categorical | Department hosting the event or 'All' |
| `previous_registrations` | Numerical | Number of campus events the student previously registered for |
| `previous_attendance` | Numerical | Number of registered events the student attended |
| `category_match` | Binary (0/1) | 1 if student's declared interests match event category/title; 0 otherwise |
| `department_match` | Binary (0/1) | 1 if event department matches student department or is campus-wide ('All') |
| `event_popularity` | Numerical | Percentage of event capacity already registered (0 to 100) |

### Training Dataset Disclosure
> ⚠️ **Dataset Disclosure**: *"Synthetic interaction data is used for initial model training because the application does not yet have sufficient historical production data."*
> 
> The initial training set contains 1,200 diverse interaction records (`ml-service/data/training_data.csv`) generated with realistic domain dynamics, student decision probability equations, and behavioral noise.

### Preprocessing & ML Pipeline
Built using scikit-learn `Pipeline` and `ColumnTransformer`:
- **Numerical Features**: Scaled using `StandardScaler` (zero mean, unit variance).
- **Categorical Features**: Encoded using `OneHotEncoder(handle_unknown='ignore', sparse_output=False)`.
- **Validation**: Stratified 80/20 train/test split preserving class distribution.

### Comparative Model Evaluation
Three candidate classification algorithms were trained and evaluated on an unseen test set ($N=240$):

| Algorithm | Accuracy | Precision | Recall | F1-Score | ROC-AUC | Status |
|-----------|----------|-----------|--------|----------|---------|--------|
| **Logistic Regression** | **93.33%** | **92.62%** | **94.17%** | **0.9339** | **0.9833** | **Selected Production Model** |
| **Random Forest** | 92.08% | 92.44% | 91.67% | 0.9205 | 0.9731 | Evaluated Alternate |
| **Decision Tree** | 88.33% | 89.66% | 86.67% | 0.8814 | 0.9414 | Evaluated Baseline |

#### Confusion Matrix for Selected Model (Logistic Regression):
```
                  Predicted Unlikely (0)   Predicted Likely (1)
Actual No (0):             111                      9
Actual Yes (1):              7                    113
```

- **Persistence**: Best model saved with `joblib` at `ml-service/models/event_registration_model.pkl`.
- **Evaluation Reports**: Generated at `ml-service/models/evaluation_report.json` and `ml-service/models/evaluation_report.txt`.

### Auxiliary ML Sentiment Classifier
An additional ML classifier is trained using **TF-IDF Vectorization (n-grams 1-2) + Logistic Regression** on campus feedback data (`ml-service/models/sentiment_model.pkl`). It provides a lightweight local ML alternative to the OpenAI sentiment analyzer.

---

## 🚀 FastAPI Microservice Endpoints

The Python microservice (`ml-service/app.py`) runs on port 8000:

| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| `GET` | `/health` | Service health status and loaded model info |
| `GET` | `/model-info` | Metadata, feature schema, and comparative evaluation metrics |
| `POST` | `/predict` | Predict registration probability for single student-event pair |
| `POST` | `/predict-batch` | Batch inference for multiple candidate events |
| `POST` | `/predict-sentiment` | ML sentiment classification via TF-IDF + Logistic Regression |

#### Sample `/predict` Request:
```json
{
  "student_department": "CSE",
  "student_year": 4,
  "event_category": "Technical",
  "event_department": "CSE",
  "previous_registrations": 5,
  "previous_attendance": 4,
  "category_match": 1,
  "department_match": 1,
  "event_popularity": 75
}
```

#### Sample `/predict` Response:
```json
{
  "prediction": 1,
  "probability": 0.9995,
  "model_used": "Logistic Regression",
  "event_id": null
}
```

---

## ☕ Spring Boot Integration

1. **HTTP Client (`MLRecommendationService`)**:
   - Uses Spring Boot 3 `RestClient` configured with connection/read timeouts.
   - Converts domain entities to `MLPredictionRequest` DTOs.
   - Dispatches requests to the FastAPI microservice.
   - **Fault Tolerance**: Catches connection failures and timeouts, logging warnings without crashing the thread.
2. **Hybrid Recommendation Engine (`RecommendationService`)**:
   - Queries `RegistrationRepository` and `AttendanceRepository` for student history.
   - Identifies candidate events the student has not yet registered for.
   - Sends features to `MLRecommendationService`.
   - If ML service is online: Ranks events by **predicted registration probability** (`probability * 100`).
   - If ML service is offline: Automatically falls back to deterministic content-based scoring and tags recommendations as `CONTENT_FALLBACK`.
3. **Generative AI Explanation Integration**:
   - The ML model determines the ranking and probability.
   - Spring Boot then calls OpenAI `RecommendationAI` to generate an individualized explanation:
     > *"Why is this recommended? Matches your technical interests in Java and your department (CSE)."*

---

## 🎨 UI Enhancements (Student Dashboard)

The Student Dashboard (`student-dashboard.html`) displays personalized event recommendations:
- **ML Recommended Badge**: `🤖 ML Predicted Pick` vs. `⭐ Curated Match` (fallback).
- **Predicted Registration Probability**: Explicitly displays probability percentage (e.g. `87% Probability`) — never mislabeled as accuracy.
- **Confidence Rating**: Visual indicator highlighted in green for high-probability events.
- **Generative AI Explanation**: Clear, readable natural language reason for the recommendation.
- **Instant Registration**: Direct registration button with real-time seat availability check.

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| **Machine Learning** | Python 3.12, scikit-learn 1.9, pandas, NumPy, joblib |
| **ML Microservice** | FastAPI 0.110, Uvicorn, Pydantic v2 |
| **Generative AI** | OpenAI Chat Completions API (`gpt-4o-mini`) via Spring `RestClient` |
| **Backend Core** | Java 17, Spring Boot 3.3.4, Spring Data JPA, Hibernate |
| **Security** | Spring Security 6 (Role-based: `ADMIN`, `STUDENT`) |
| **Frontend** | Thymeleaf, Bootstrap 5.3, Bootstrap Icons, JavaScript (ES6) |
| **Database** | MySQL 8.0 |
| **Build & Dependency** | Maven (`./mvnw`), Python `uv` / `pip` |

---

## 🗃️ Database Entities

```
students        → id, name, email, password, department, year, interests, role
events          → id, title, description, category, department, eventDate, eventTime,
                   venue, capacity, organizer, status, aiFeedbackSummary
registrations   → id, student_id (FK), event_id (FK), registrationDate, status
attendance      → id, registration_id (FK), attended, markedAt
feedback        → id, student_id (FK), event_id (FK), comment, rating,
                   sentiment, sentimentScore, submittedAt
```

---

## ⚙️ How to Run the Complete System

### Step 1: Start MySQL Database
Ensure MySQL is running on port 3306 with the `smart_campus` database created:
```sql
CREATE DATABASE smart_campus;
```

### Step 2: Start the Python FastAPI ML Microservice
Open a terminal in the project root:
```bash
# If using the pre-configured virtual environment:
ml-service/.venv/Scripts/uvicorn.exe app:app --app-dir ml-service --host 127.0.0.1 --port 8000

# Or with uv / standard python:
cd ml-service
python -m uvicorn app:app --host 127.0.0.1 --port 8000
```
Verify the ML service at: **http://127.0.0.1:8000/health**

### Step 3: Configure Spring Boot (Optional OpenAI Key)
Set your OpenAI API key in your environment if you want live Generative AI responses (fallback heuristics are used if unset):
```powershell
# Windows PowerShell
$env:OPENAI_API_KEY="your-key-here"

# Linux / macOS
export OPENAI_API_KEY="your-key-here"
```

### Step 4: Run Spring Boot Application
In another terminal in the project root:
```bash
./mvnw.cmd spring-boot:run
```

### Step 5: Access the Web Application
Open your browser at: **http://localhost:8080/login**

#### Demo Credentials:
| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@smartcampus.com` | `admin123` |
| **Student** | `student@smartcampus.com` | `student123` |

---

## 🎓 Interview-Ready Project Explanation

> *"My project combines Machine Learning and Generative AI. I built a supervised machine learning model that predicts whether a student is likely to register for an event based on student profile and historical interaction features. I trained and evaluated the model using scikit-learn and exposed it through a FastAPI service. My Spring Boot application communicates with this ML service and displays personalized event recommendations. I also integrated OpenAI's Generative AI for feedback summarization, sentiment analysis, and natural-language explanations of recommendations."*

---

*Developed as a Final-Year B.Tech CSE Project — CampuSphere*

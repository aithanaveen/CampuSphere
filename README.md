# CampuSphere AI — Smart Campus Event Management System

A **Final-Year B.Tech CSE Project** demonstrating full-stack Java web development with **Generative AI integration**.

---

## 📌 Project Description

A complete web application that allows students to discover, register for, and provide feedback on campus events — while giving administrators powerful tools to manage events and analyze AI-powered feedback insights.

---

## ✨ Features

### 👤 Student Portal
- Secure login with role-based routing
- Student profile display (name, department, year, interests)
- Browse all available campus events
- AI-powered personalized event recommendations
- One-click event registration with capacity enforcement
- Feedback submission with **AI sentiment analysis** (POSITIVE / NEGATIVE / NEUTRAL)
- View all personal registrations

### 🛡️ Admin Panel
- Secure admin login (separate dashboard)
- Create, Edit, Delete campus events
- View all registrations per event
- Mark student attendance (present/absent)
- View all student feedback with sentiment badges
- **AI-generated event feedback summary** (on demand)
- Sentiment analytics dashboard (positive / negative / neutral counts + percentages)

---

## 🧠 AI Features

| Feature | Description | Technology |
|---------|-------------|------------|
| **Sentiment Analysis** | Analyzes student feedback comments | OpenAI GPT-4o-mini via Chat API |
| **Sentiment Score** | Confidence score 0.0–1.0 for sentiment | OpenAI GPT-4o-mini |
| **Feedback Summary** | Admin-triggered AI summary of all event feedback | OpenAI GPT-4o-mini |
| **Recommendation Explanation** | One-sentence AI reason why an event is recommended | OpenAI GPT-4o-mini |
| **Content-Based Scoring** | Deterministic scoring algorithm (NOT machine learning) | Java (custom algorithm) |

> ⚠️ **Honest Disclosure**: The recommendation engine uses a **deterministic content-based scoring algorithm** (interest matching + department + year). AI generates the human-readable explanation only. This is NOT a machine learning model.

### AI Fallback Behavior
If the OpenAI API is unavailable:
- Sentiment falls back to **NEUTRAL (0.5)** — feedback submission never fails
- Recommendations still work with a deterministic fallback explanation
- Feedback summary shows a clear message instead of crashing

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.3.4 |
| ORM | Spring Data JPA, Hibernate |
| Security | Spring Security (HTTP Basic Auth) |
| Frontend | HTML, CSS, Bootstrap 5.3, JavaScript |
| Templating | Thymeleaf |
| Database | MySQL 8.x |
| AI API | OpenAI Chat Completions API (gpt-4o-mini) |
| Build | Apache Maven |

---

## 🏗️ Architecture

```
Browser (HTML/CSS/JS + Bootstrap)
         ↕  HTTP Basic Auth + REST JSON
Spring Boot 3.3.4
         ↕
Spring Security (Role-based: ADMIN / STUDENT)
         ↕
Controllers (thin) → Services (business logic) → Repositories (JPA)
         ↕                    ↕
        MySQL            AI Layer (OpenAI Chat API)
```

---

## 🗃️ Database Entities

```
students        → id, name, email, password, department, year, interests, role
events          → id, title, description, category, department, eventDate, eventTime,
                   venue, capacity, organizer, aiFeedbackSummary
registrations   → id, student_id (FK), event_id (FK), registrationDate, status
attendance      → id, registration_id (FK), attended, markedAt
feedback        → id, student_id (FK), event_id (FK), comment, rating,
                   sentiment, sentimentScore, submittedAt
```

### Relationships
- `Registration` → `Student` (ManyToOne), `Event` (ManyToOne)
- `Attendance` → `Registration` (OneToOne)
- `Feedback` → `Student` (ManyToOne), `Event` (ManyToOne)

---

## 🔐 Security Model

| Endpoint | Role Required |
|----------|--------------|
| `GET /api/events/**` | Public |
| `POST /api/students` | Public (self-registration) |
| `POST /api/events/**` | ADMIN |
| `PUT /api/events/**` | ADMIN |
| `DELETE /api/events/**` | ADMIN |
| `POST /api/attendance/**` | ADMIN |
| `POST /api/sentiment/**` | ADMIN |
| `GET /api/students/**` | ADMIN |
| `POST /api/registrations/event/**` | STUDENT |
| `GET /api/registrations/my` | STUDENT |
| `POST /api/feedback/**` | STUDENT |
| `GET /api/recommendations/**` | STUDENT |
| `/api/auth/me` | Authenticated |

---

## 📡 API Endpoints

### Auth
```
GET  /api/auth/me              → Returns {id, email, name, role, department, interests, year}
```

### Events
```
GET    /api/events             → List all events (public)
GET    /api/events/{id}        → Get event by ID (public)
POST   /api/events             → Create event (ADMIN)
PUT    /api/events/{id}        → Update event (ADMIN)
DELETE /api/events/{id}        → Delete event (ADMIN)
```

### Registrations
```
POST /api/registrations/event/{eventId}    → Register for event (STUDENT)
GET  /api/registrations/my                 → My registrations (STUDENT)
GET  /api/registrations/event/{eventId}    → Event's registrations (ADMIN)
```

### Attendance
```
POST /api/attendance/registration/{id}?attended=true  → Mark attendance (ADMIN)
GET  /api/attendance/registration/{id}                → Get attendance (ADMIN)
```

### Feedback
```
POST /api/feedback/event/{eventId}?rating=4&comment=... → Submit feedback (STUDENT)
GET  /api/feedback/event/{eventId}                       → Event feedback (Authenticated)
GET  /api/feedback/my                                    → My feedback (Student)
```

### Sentiment & AI
```
GET  /api/sentiment/event/{eventId}         → Sentiment analytics (Authenticated)
POST /api/sentiment/event/{eventId}/summary → Generate AI summary (ADMIN)
```

### Recommendations
```
GET /api/recommendations  → Personalized recommendations (STUDENT)
```

---

## ⚙️ How to Run

### Prerequisites
- Java 17+
- MySQL 8.x running on port 3306
- Maven 3.8+
- OpenAI API Key 

### 1. Create MySQL Database
```sql
CREATE DATABASE smart_campus;
```

### 2. Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_campus
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Set OpenAI API Key (Optional)
```bash
# Windows
set OPENAI_API_KEY=sk-...your-key-here...

# Linux/Mac
export OPENAI_API_KEY=sk-...your-key-here...
```

> Without the API key, all AI features degrade gracefully:
> - Sentiment → NEUTRAL (0.5) fallback
> - Summary → informative message
> - Recommendation reason → deterministic text

### 4. Run the Application
```bash
mvn spring-boot:run
```

### 5. Access the App
Open: **http://localhost:8080/login**

---

## 🔑 Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@smartcampus.com | admin123 |
| Student | student@smartcampus.com | student123 |

> These accounts are auto-created by `DataInitializer` when the app first starts.

---

## 📋 Demo Events (Auto-Created)

| Event | Category | Capacity |
|-------|----------|----------|
| AI & Machine Learning Workshop | Technology | 80 |
| Java Full Stack Development Bootcamp | Technology | 60 |
| Cloud Computing & DevOps Seminar | Technology | 150 |
| National Level Hackathon 2026 | Competition | 200 |
| Annual Sports Meet 2026 | Sports | 500 |
| Cultural Fest — Tarang 2026 | Cultural | 1000 |

---

## 🔮 Future Enhancements

1. **JWT Token Auth** — Replace HTTP Basic with stateless JWT tokens
2. **Email Notifications** — Send confirmation emails on registration
3. **Real ML Model** — Replace content-based scoring with actual collaborative filtering
4. **QR Code Attendance** — Generate and scan QR codes for attendance
5. **Calendar Integration** — Export events to Google Calendar / Outlook
6. **Student Department Dashboard** — Per-department event analytics
7. **Event Images** — Upload and display event banners

---


*Developed as a Final-Year B.Tech CSE Project*

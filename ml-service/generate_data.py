"""
Script to generate synthetic historical student-event interaction training data.

DISCLOSURE & DOCUMENTATION:
"Synthetic interaction data is used for initial model training because the
application does not yet have sufficient historical production data."

Dataset features:
- student_id: identifier of student
- event_id: identifier of event
- student_department: Department of student (e.g. CSE, ECE, IT, MECH, CIVIL)
- student_year: Year of study (1, 2, 3, 4)
- event_category: Category of event (Technical, Workshop, Cultural, Seminar, Hackathon, Sports)
- event_department: Target/organizing department (CSE, ECE, IT, MECH, CIVIL, All)
- previous_registrations: Number of events student registered for previously (0-15)
- previous_attendance: Number of events student actually attended (0-15)
- category_match: Binary (1 if event category matches student interests, 0 otherwise)
- department_match: Binary (1 if event department matches student department or is 'All', 0 otherwise)
- event_popularity: Percentage of capacity filled / popularity index (0-100)
- registered: Target binary label (1 = registered, 0 = did not register)
"""

import os
import random
import numpy as np
import pandas as pd

def generate_interaction_dataset(num_samples: int = 1200, seed: int = 42) -> pd.DataFrame:
    random.seed(seed)
    np.random.seed(seed)

    departments = ["CSE", "ECE", "IT", "MECH", "CIVIL", "AI&DS"]
    event_departments = ["CSE", "ECE", "IT", "MECH", "CIVIL", "AI&DS", "All"]
    categories = ["Technical", "Workshop", "Cultural", "Seminar", "Hackathon", "Sports"]

    records = []

    for i in range(num_samples):
        student_id = random.randint(1, 150)
        event_id = random.randint(1, 40)
        
        student_dept = random.choice(departments)
        student_year = random.choice([1, 2, 3, 4])
        
        event_cat = random.choice(categories)
        # Event department often matches or is 'All'
        if random.random() < 0.4:
            event_dept = student_dept
        elif random.random() < 0.3:
            event_dept = "All"
        else:
            event_dept = random.choice(event_departments)
            
        # Past participation features
        prev_regs = max(0, int(np.random.poisson(lam=4.0)))
        if prev_regs == 0:
            prev_att = 0
        else:
            att_rate = np.random.beta(a=5, b=2)  # Typically students attend ~70% of registered
            prev_att = min(prev_regs, int(round(prev_regs * att_rate)))

        # Domain matching logic
        dept_match = 1 if (event_dept == student_dept or event_dept == "All") else 0
        
        # Category match probability (student interest alignment)
        # Senior students in tech departments more likely to match technical/hackathons
        if student_dept in ["CSE", "IT", "AI&DS"] and event_cat in ["Technical", "Workshop", "Hackathon"]:
            cat_match = 1 if random.random() < 0.75 else 0
        else:
            cat_match = 1 if random.random() < 0.40 else 0

        # Event popularity (0 to 100)
        popularity = int(np.clip(np.random.normal(loc=55, scale=20), 10, 100))

        # Logistic probability equation simulating human student decision
        # Log-odds based on domain rules + stochastic noise
        log_odds = (
            -3.2
            + 1.8 * cat_match
            + 1.4 * dept_match
            + 0.15 * prev_regs
            + 0.22 * prev_att
            + 0.02 * (popularity - 50)
            + 0.15 * (student_year - 2)
            + np.random.normal(0, 0.45)  # Realistic noise
        )
        
        probability = 1.0 / (1.0 + np.exp(-log_odds))
        registered = 1 if probability >= 0.50 else 0

        records.append({
            "student_id": student_id,
            "event_id": event_id,
            "student_department": student_dept,
            "student_year": student_year,
            "event_category": event_cat,
            "event_department": event_dept,
            "previous_registrations": prev_regs,
            "previous_attendance": prev_att,
            "category_match": cat_match,
            "department_match": dept_match,
            "event_popularity": popularity,
            "registered": registered
        })

    df = pd.DataFrame(records)
    return df


def generate_feedback_sentiment_dataset(seed: int = 42) -> pd.DataFrame:
    """
    Synthetic dataset of student feedback comments for training a TF-IDF + Logistic Regression
    sentiment classifier.
    """
    random.seed(seed)
    
    positive_samples = [
        "The workshop was extremely informative and well structured.",
        "Great hands-on coding experience, the mentor was very helpful.",
        "Loved the hackathon organization and the problem statements.",
        "Excellent event! Learned so much about modern AI and cloud architectures.",
        "Wonderful guest lecture with clear explanations and real world examples.",
        "Outstanding coordination and great refreshments provided.",
        "Really enjoyed the interactive coding sessions and team challenges.",
        "Best technical workshop of the semester, highly recommend!",
        "Very inspiring session by the industry speakers.",
        "Superb arrangement, everything was on schedule and informative.",
        "The practical demonstrations helped solidify key concepts.",
        "Had a great time collaborating with students from other branches.",
        "Fabulous presentation and well prepared speaker notes.",
        "Engaging and energetic speaker, kept everyone attentive throughout.",
        "Very satisfied with the learning outcomes and certificates.",
        "The session was fantastic, awesome presentation and well organized!",
        "Amazing workshop, very helpful mentors and great content.",
        "Highly impressed with the event organization and learning opportunities."
    ]

    negative_samples = [
        "Very poorly organized, started almost an hour late.",
        "The audio system was terrible and the speaker was not audible at the back.",
        "Complete waste of time, the content was too basic and superficial.",
        "Disappointed with the lack of hands-on practice, mostly boring slides.",
        "The venue was overcrowded and the air conditioning was not working.",
        "Rushed through important topics without giving time for questions.",
        "Unhelpful organizers and confusing instructions for the competition.",
        "Wi-Fi was down throughout the lab session, couldn't finish the tasks.",
        "Expected advanced concepts but it was just introductory basics.",
        "Terrible experience, certificates were not distributed as promised.",
        "The workshop description was misleading compared to actual content.",
        "Disorganized team and no clear schedule provided.",
        "Boring presentation with very little student interaction.",
        "Not worth attending, poorly managed event.",
        "Bad audio quality, awful coordination and unresponsive coordinators.",
        "Horrible experience, completely disorganized and useless."
    ]

    neutral_samples = [
        "The event was average, covered standard topics as expected.",
        "Decent workshop, some parts were good while others were theoretical.",
        "It was okay, good for first year students who are new to coding.",
        "Standard seminar, presentation slides could be shared earlier.",
        "The session was fine, but timing could be adjusted better.",
        "Covered the syllabus topics, nothing particularly new or bad.",
        "Average arrangement, neither too impressive nor terrible.",
        "Acceptable presentation, speaker covered the scheduled agenda.",
        "An ordinary college seminar with standard slides.",
        "Fair overview of the subject, reasonable for beginners.",
        "Neutral impression, event proceeded as described in the flyer.",
        "Satisfactory session, would prefer more practical examples next time.",
        "Mediocre experience, but learned a few useful definitions.",
        "Event took place as scheduled, average attendee engagement.",
        "General introduction, met standard expectations."
    ]

    records = []
    # Expand by sampling variations
    for _ in range(150):
        records.append({"comment": random.choice(positive_samples), "sentiment": "POSITIVE"})
        records.append({"comment": random.choice(negative_samples), "sentiment": "NEGATIVE"})
        records.append({"comment": random.choice(neutral_samples), "sentiment": "NEUTRAL"})

    df = pd.DataFrame(records)
    return df


if __name__ == "__main__":
    os.makedirs("ml-service/data", exist_ok=True)
    
    # 1. Generate interaction dataset
    interactions_df = generate_interaction_dataset(num_samples=1200)
    interaction_path = "ml-service/data/training_data.csv"
    interactions_df.to_csv(interaction_path, index=False)
    print(f"Generated {len(interactions_df)} interaction samples -> {interaction_path}")
    print(f"Target distribution:\n{interactions_df['registered'].value_counts(normalize=True)}")
    
    # 2. Generate feedback sentiment dataset
    sentiment_df = generate_feedback_sentiment_dataset()
    sentiment_path = "ml-service/data/feedback_sentiment_data.csv"
    sentiment_df.to_csv(sentiment_path, index=False)
    print(f"Generated {len(sentiment_df)} sentiment samples -> {sentiment_path}")
    print(f"Sentiment distribution:\n{sentiment_df['sentiment'].value_counts()}")

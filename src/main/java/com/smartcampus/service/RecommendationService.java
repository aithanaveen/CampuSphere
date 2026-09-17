package com.smartcampus.service;

import com.smartcampus.ai.RecommendationAI;
import com.smartcampus.dto.MLPredictionRequest;
import com.smartcampus.dto.MLPredictionResponse;
import com.smartcampus.dto.RecommendationDTO;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.AttendanceRepository;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import com.smartcampus.repository.StudentRepository;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

/**
 * Hybrid Machine Learning & Content-Based Event Recommendation Service.
 *
 * ARCHITECTURE & ML FLOW:
 * 1. Data/Feature Extraction:
 *    - Student features: department, year, interests, past registrations, past attendance
 *    - Event features: category, department, capacity, registration popularity
 *    - Interaction features: category_match (interests match event category),
 *      department_match (student department matches event department)
 * 2. Machine Learning Inference:
 *    - Queries the Python FastAPI microservice (trained scikit-learn Logistic Regression / Random Forest model)
 *    - Model outputs a true predicted registration probability (0.0 to 1.0) and binary prediction (1/0)
 * 3. Graceful Fallback:
 *    - If the ML microservice is offline or fails, seamlessly falls back to the deterministic
 *      content-based relevance scoring algorithm so recommendations are NEVER broken.
 * 4. Generative AI Explanation:
 *    - After ML predicts the probability, OpenAI GPT-4o-mini generates a natural language explanation
 *      justifying why the event is recommended for the student.
 */
@Service
public class RecommendationService {

    private static final Logger log = Logger.getLogger(RecommendationService.class.getName());

    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final RecommendationAI recommendationAI;
    private final RegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final MLRecommendationService mlRecommendationService;

    public RecommendationService(
            StudentRepository studentRepository,
            EventRepository eventRepository,
            RecommendationAI recommendationAI,
            RegistrationRepository registrationRepository,
            AttendanceRepository attendanceRepository,
            MLRecommendationService mlRecommendationService) {

        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.recommendationAI = recommendationAI;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.mlRecommendationService = mlRecommendationService;
    }

    public List<RecommendationDTO> getRecommendations(String email) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Event> events = eventRepository.findAll();

        // Get IDs of events the student already registered for — exclude from recommendations
        Set<Long> registeredEventIds = registrationRepository.findByStudentId(student.getId())
                .stream()
                .map(r -> r.getEvent().getId())
                .collect(java.util.stream.Collectors.toSet());

        Set<String> interests = extractInterests(student.getInterests());

        // Historical interaction behavior of the student
        int prevRegistrations = registrationRepository.findByStudentId(student.getId()).size();
        int prevAttendance = (int) attendanceRepository.countAttendedByStudentId(student.getId());

        List<RecommendationDTO> recommendations = new ArrayList<>();

        for (Event event : events) {
            // Skip events the student is already registered for
            if (registeredEventIds.contains(event.getId())) {
                continue;
            }

            // 1. Feature Engineering
            int categoryMatch = isCategoryMatched(interests, event.getCategory()) ? 1 : 0;
            int departmentMatch = isDepartmentMatched(student.getDepartment(), event.getDepartment(), event.getOrganizer()) ? 1 : 0;
            int eventPopularity = calculateEventPopularity(event);

            MLPredictionRequest mlRequest = new MLPredictionRequest(
                    student.getDepartment() != null ? student.getDepartment() : "General",
                    student.getYear() != null ? student.getYear() : 1,
                    event.getCategory() != null ? event.getCategory() : "General",
                    event.getDepartment() != null ? event.getDepartment() : "All",
                    prevRegistrations,
                    prevAttendance,
                    categoryMatch,
                    departmentMatch,
                    eventPopularity,
                    event.getId()
            );

            // 2. Query ML Microservice for prediction
            MLPredictionResponse mlResponse = mlRecommendationService.predictRegistration(mlRequest);

            double score;
            double probability;
            int prediction;
            String modelSource;

            if (mlResponse != null && mlResponse.getProbability() != null) {
                // Genuine ML model prediction
                probability = mlResponse.getProbability();
                prediction = mlResponse.getPrediction();
                score = Math.round(probability * 100.0);
                modelSource = "ML_MODEL";
            } else {
                // Graceful fallback: Deterministic content-based scoring
                score = calculateFallbackScore(student, interests, event);
                probability = score > 0 ? Math.min(0.95, 0.50 + (score / 200.0)) : 0.20;
                prediction = score > 0 ? 1 : 0;
                modelSource = "CONTENT_FALLBACK";
            }

            // 3. Generative AI Explanation (OpenAI GPT-4o-mini with local heuristic fallback)
            String reason = recommendationAI.generateReason(
                    student.getInterests(),
                    student.getDepartment(),
                    student.getYear() != null ? student.getYear().toString() : "N/A",
                    event.getTitle(),
                    event.getCategory(),
                    event.getDescription()
            );

            recommendations.add(new RecommendationDTO(event, score, reason, probability, prediction, modelSource));
        }

        // Sort by predicted probability descending (highest probability first)
        recommendations.sort(Comparator.comparing(RecommendationDTO::getProbability).reversed());

        return recommendations;
    }

    /**
     * Checks if any student interest matches the event category or title keywords.
     */
    private boolean isCategoryMatched(Set<String> interests, String eventCategory) {
        if (eventCategory == null || interests.isEmpty()) {
            return false;
        }
        String lowerCat = eventCategory.toLowerCase();
        for (String interest : interests) {
            if (lowerCat.contains(interest) || interest.contains(lowerCat)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if student department matches the event department or organizer.
     */
    private boolean isDepartmentMatched(String studentDept, String eventDept, String organizer) {
        if (studentDept == null) return false;
        String sDept = studentDept.trim().toLowerCase();

        if (eventDept != null) {
            String eDept = eventDept.trim().toLowerCase();
            if (eDept.equals("all") || eDept.contains(sDept) || sDept.contains(eDept)) {
                return true;
            }
        }

        if (organizer != null) {
            String org = organizer.trim().toLowerCase();
            if (org.contains(sDept) || sDept.contains(org)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Estimates event popularity index (0 to 100) based on current registrations / capacity.
     */
    private int calculateEventPopularity(Event event) {
        long regCount = registrationRepository.countByEventId(event.getId());
        int capacity = event.getCapacity() != null && event.getCapacity() > 0 ? event.getCapacity() : 100;
        int pct = (int) ((regCount * 100L) / capacity);
        return Math.min(100, Math.max(10, pct));
    }

    /**
     * Deterministic content-based relevance score used as graceful fallback when ML service is offline.
     */
    private double calculateFallbackScore(Student student, Set<String> interests, Event event) {
        double score = 0;

        for (String interest : interests) {
            if (event.getCategory() != null && event.getCategory().toLowerCase().contains(interest)) {
                score += 50;
            }
            if (event.getTitle() != null && event.getTitle().toLowerCase().contains(interest)) {
                score += 30;
            }
            if (event.getDescription() != null && event.getDescription().toLowerCase().contains(interest)) {
                score += 20;
            }
        }

        if (student.getDepartment() != null) {
            String dept = student.getDepartment().toLowerCase();
            if (event.getOrganizer() != null && event.getOrganizer().toLowerCase().contains(dept)) {
                score += 15;
            }
            if (event.getDepartment() != null && event.getDepartment().toLowerCase().contains(dept)) {
                score += 15;
            }
        }

        if (student.getYear() != null) {
            String yearStr = student.getYear().toString();
            if (event.getTitle() != null && event.getTitle().contains(yearStr)) {
                score += 10;
            }
            if (event.getDescription() != null && event.getDescription().contains(yearStr)) {
                score += 10;
            }
        }

        return score;
    }

    private Set<String> extractInterests(String interestString) {
        Set<String> interests = new HashSet<>();
        if (interestString == null || interestString.isBlank()) {
            return interests;
        }
        Arrays.stream(interestString.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(interests::add);
        return interests;
    }
}
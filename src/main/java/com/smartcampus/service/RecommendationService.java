package com.smartcampus.service;

import com.smartcampus.ai.RecommendationAI;
import com.smartcampus.dto.RecommendationDTO;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import com.smartcampus.repository.StudentRepository;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Content-based event recommendation service.
 *
 * HOW IT WORKS:
 * - Calculates a deterministic relevance score for each event based on:
 *   - Matching student interests with event category, title, and description
 *   - Department relevance (student department matches event organizer or department)
 *   - Year relevance (events explicitly mentioning the student's year)
 * - Events are sorted by score (highest first)
 * - Only events with score > 0 are included in the results
 *   (all events are shown if none match, to avoid empty recommendations)
 * - AI generates a one-sentence human-readable explanation for each recommendation
 * - If AI is unavailable, a deterministic fallback reason is used
 *
 * NOTE: This is a deterministic content-based recommendation algorithm.
 * It is NOT machine learning.
 */
@Service
public class RecommendationService {

    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final RecommendationAI recommendationAI;
    private final RegistrationRepository registrationRepository;

    public RecommendationService(
            StudentRepository studentRepository,
            EventRepository eventRepository,
            RecommendationAI recommendationAI,
            RegistrationRepository registrationRepository) {

        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.recommendationAI = recommendationAI;
        this.registrationRepository = registrationRepository;
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

        List<RecommendationDTO> recommendations = new ArrayList<>();

        for (Event event : events) {
            // Skip events the student is already registered for
            if (registeredEventIds.contains(event.getId())) {
                continue;
            }

            double score = calculateScore(student, interests, event);

            // Generate AI explanation (never throws — fallback built in)
            String reason = recommendationAI.generateReason(
                    student.getInterests(),
                    student.getDepartment(),
                    student.getYear() != null ? student.getYear().toString() : "N/A",
                    event.getTitle(),
                    event.getCategory(),
                    event.getDescription()
            );

            recommendations.add(new RecommendationDTO(event, score, reason));
        }

        // Sort by score descending
        recommendations.sort(Comparator.comparing(RecommendationDTO::getScore).reversed());

        // If some events scored > 0, return only those; otherwise return all (no empty recommendations)
        List<RecommendationDTO> relevant = recommendations.stream()
                .filter(r -> r.getScore() > 0)
                .toList();

        return relevant.isEmpty() ? recommendations : relevant;
    }

    /**
     * Calculate content-based relevance score for a student–event pair.
     *
     * Scoring breakdown:
     *   +50 per interest that matches the event category
     *   +30 per interest that matches the event title
     *   +20 per interest that matches the event description
     *   +15 if student department matches event organizer or event department
     *   +10 if student year is mentioned in the event title or description
     */
    private double calculateScore(Student student, Set<String> interests, Event event) {

        double score = 0;

        for (String interest : interests) {

            if (event.getCategory() != null &&
                    event.getCategory().toLowerCase().contains(interest)) {
                score += 50;
            }

            if (event.getTitle() != null &&
                    event.getTitle().toLowerCase().contains(interest)) {
                score += 30;
            }

            if (event.getDescription() != null &&
                    event.getDescription().toLowerCase().contains(interest)) {
                score += 20;
            }
        }

        // Department relevance
        if (student.getDepartment() != null) {
            String dept = student.getDepartment().toLowerCase();

            if (event.getOrganizer() != null &&
                    event.getOrganizer().toLowerCase().contains(dept)) {
                score += 15;
            }

            if (event.getDepartment() != null &&
                    event.getDepartment().toLowerCase().contains(dept)) {
                score += 15;
            }
        }

        // Year relevance — events that mention the student's year
        if (student.getYear() != null) {
            String yearStr = student.getYear().toString();

            if (event.getTitle() != null &&
                    event.getTitle().contains(yearStr)) {
                score += 10;
            }

            if (event.getDescription() != null &&
                    event.getDescription().contains(yearStr)) {
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
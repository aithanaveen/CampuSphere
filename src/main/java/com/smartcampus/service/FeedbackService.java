package com.smartcampus.service;

import com.smartcampus.ai.FeedbackSummarizer;
import com.smartcampus.ai.SentimentAnalyzer;
import com.smartcampus.ai.SentimentResult;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Feedback;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.FeedbackRepository;
import com.smartcampus.repository.StudentRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final FeedbackSummarizer feedbackSummarizer;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            StudentRepository studentRepository,
            EventRepository eventRepository,
            SentimentAnalyzer sentimentAnalyzer,
            FeedbackSummarizer feedbackSummarizer) {

        this.feedbackRepository = feedbackRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.feedbackSummarizer = feedbackSummarizer;
    }

    /**
     * Submit feedback for an event.
     * The student is identified by their authenticated email — NOT by a supplied student ID.
     * This prevents one student from submitting feedback impersonating another.
     *
     * AI sentiment analysis is performed on the comment text.
     * If AI is unavailable, NEUTRAL 0.5 is stored as a safe fallback.
     */
    public Feedback submitFeedback(String email, Long eventId, Integer rating, String comment) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Feedback feedback = new Feedback();
        feedback.setStudent(student);
        feedback.setEvent(event);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setSubmittedAt(LocalDateTime.now());

        // AI sentiment analysis — falls back to NEUTRAL on failure, never crashes
        SentimentResult sentimentResult = sentimentAnalyzer.analyze(comment);
        feedback.setSentiment(sentimentResult.getSentiment());
        feedback.setSentimentScore(sentimentResult.getScore());

        return feedbackRepository.save(feedback);
    }

    /**
     * Get all feedback submitted by the currently authenticated student.
     */
    public List<Feedback> getMyFeedback(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return feedbackRepository.findByStudentId(student.getId());
    }

    /**
     * Get all feedback submitted by a student by ID. (Admin use)
     */
    public List<Feedback> getStudentFeedback(Long studentId) {
        return feedbackRepository.findByStudentId(studentId);
    }

    /**
     * Get all feedback for a specific event.
     */
    public List<Feedback> getEventFeedback(Long eventId) {
        return feedbackRepository.findByEventId(eventId);
    }

    /**
     * Get all feedback across all events. (Admin use)
     */
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    /**
     * Generate an AI summary for all feedback comments on an event.
     * Saves the summary to Event.aiFeedbackSummary.
     * Only called explicitly by an admin — not auto-generated on every feedback submission.
     *
     * If there is no feedback, returns an informative message.
     * If AI fails, returns the fallback message from FeedbackSummarizer.
     */
    public String generateEventSummary(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<Feedback> feedbackList = feedbackRepository.findByEventId(eventId);

        if (feedbackList.isEmpty()) {
            return "No feedback has been submitted for this event yet.";
        }

        StringBuilder feedbackText = new StringBuilder();

        for (Feedback feedback : feedbackList) {
            if (feedback.getComment() != null && !feedback.getComment().isBlank()) {
                feedbackText.append("- ").append(feedback.getComment()).append("\n");
            }
        }

        if (feedbackText.isEmpty()) {
            return "No written comments are available for this event.";
        }

        String summary = feedbackSummarizer.generateSummary(feedbackText.toString());

        // Persist summary on the Event entity
        event.setAiFeedbackSummary(summary);
        eventRepository.save(event);

        return summary;
    }
}

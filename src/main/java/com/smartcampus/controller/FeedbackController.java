package com.smartcampus.controller;

import com.smartcampus.entity.Feedback;
import com.smartcampus.service.FeedbackService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feedback controller.
 * Students submit feedback using their authenticated email —
 * no student ID is provided by the frontend.
 * This prevents impersonation of other students.
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * Student submits feedback for an event.
     * The authenticated user's email is used to identify the student.
     * POST /api/feedback/event/{eventId}?rating=4&comment=Great+event
     */
    @PostMapping("/event/{eventId}")
    public ResponseEntity<Feedback> submitFeedback(
            @PathVariable Long eventId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            Authentication authentication) {

        String email = authentication.getName();
        Feedback feedback = feedbackService.submitFeedback(email, eventId, rating, comment);
        return ResponseEntity.ok(feedback);
    }

    /**
     * Get all feedback submitted by the currently authenticated student.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Feedback>> getMyFeedback(Authentication authentication) {
        String email = authentication.getName();
        List<Feedback> feedbackList = feedbackService.getMyFeedback(email);
        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Get all feedback for a specific event. (Admin or authenticated users)
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Feedback>> getEventFeedback(@PathVariable Long eventId) {
        List<Feedback> feedbackList = feedbackService.getEventFeedback(eventId);
        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Admin views all feedback for a specific student.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Feedback>> getStudentFeedback(@PathVariable Long studentId) {
        List<Feedback> feedbackList = feedbackService.getStudentFeedback(studentId);
        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Admin views all feedback across all events.
     */
    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        return ResponseEntity.ok(feedbackList);
    }
}
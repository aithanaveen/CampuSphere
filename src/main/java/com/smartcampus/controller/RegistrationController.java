package com.smartcampus.controller;

import com.smartcampus.entity.Registration;
import com.smartcampus.service.RegistrationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Student registers for an event.
     * Uses the authenticated user's email — no student ID needed from the frontend.
     */
    @PostMapping("/event/{eventId}")
    public ResponseEntity<Registration> registerStudent(
            @PathVariable Long eventId,
            Authentication authentication) {

        String email = authentication.getName();
        Registration registration = registrationService.registerStudent(email, eventId);
        return ResponseEntity.ok(registration);
    }

    /**
     * Student retrieves their own registrations.
     * Authenticated email determines identity — prevents accessing another student's data.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Registration>> getMyRegistrations(
            Authentication authentication) {

        String email = authentication.getName();
        List<Registration> registrations = registrationService.getMyRegistrations(email);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Get registrations of a student by ID. (Admin use only — secured in SecurityConfig)
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Registration>> getStudentRegistrations(
            @PathVariable Long studentId) {

        List<Registration> registrations = registrationService.getStudentRegistrations(studentId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Admin views all registrations for an event.
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registration>> getEventRegistrations(
            @PathVariable Long eventId) {

        List<Registration> registrations = registrationService.getEventRegistrations(eventId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Student withdraws from a registered event.
     * Only the student who registered can cancel their own registration.
     */
    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> withdrawRegistration(
            @PathVariable Long registrationId,
            Authentication authentication) {

        String email = authentication.getName();
        registrationService.withdrawRegistration(email, registrationId);
        return ResponseEntity.ok().build();
    }
}
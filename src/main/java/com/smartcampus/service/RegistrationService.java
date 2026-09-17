package com.smartcampus.service;

import com.smartcampus.entity.Event;
import com.smartcampus.entity.Registration;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import com.smartcampus.repository.StudentRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            StudentRepository studentRepository,
            EventRepository eventRepository) {

        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Register the currently authenticated student for an event.
     * Enforces duplicate-registration prevention and capacity limit.
     */
    public Registration registerStudent(String email, Long eventId) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Prevent duplicate registration
        if (registrationRepository.existsByStudentIdAndEventId(
                student.getId(), event.getId())) {
            throw new RuntimeException("You are already registered for this event");
        }

        // Check capacity
        if (event.getCapacity() != null) {
            long currentRegistrations = registrationRepository.countByEventId(event.getId());
            if (currentRegistrations >= event.getCapacity()) {
                throw new RuntimeException("Event is full. No more registrations are allowed.");
            }
        }

        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setEvent(event);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setStatus("REGISTERED");

        return registrationRepository.save(registration);
    }

    /**
     * Get all registrations of the currently authenticated student.
     * Uses email from Spring Security — student cannot access another student's registrations.
     */
    public List<Registration> getMyRegistrations(String email) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return registrationRepository.findByStudentId(student.getId());
    }

    /**
     * Get registrations of a student by ID. (Admin use)
     */
    public List<Registration> getStudentRegistrations(Long studentId) {
        return registrationRepository.findByStudentId(studentId);
    }

    /**
     * Get all registrations for a specific event. (Admin use)
     */
    public List<Registration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    /**
     * Withdraw (cancel) a student's own registration.
     * Validates that the authenticated student owns this registration before deleting.
     */
    public void withdrawRegistration(String email, Long registrationId) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        // Security: only the owning student may withdraw
        if (!registration.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("You are not authorized to withdraw this registration");
        }

        registrationRepository.deleteById(registrationId);
    }
}
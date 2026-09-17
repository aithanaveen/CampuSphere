package com.smartcampus.controller;

import com.smartcampus.dto.UserProfileDTO;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller.
 * Provides the /api/auth/me endpoint and /api/auth/forgot-password.
 */
@RestController
public class AuthController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || email.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and new password are required."));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters."));
        }

        Student student = studentRepository.findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (student == null) {
            student = studentRepository.findAll().stream()
                    .filter(s -> s.getEmail() != null && s.getEmail().equalsIgnoreCase(email.trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (student == null) {
            return ResponseEntity.status(404).body(Map.of("message", "No account registered with email: " + email));
        }

        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully! You can now log in."));
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<UserProfileDTO> currentUser(Authentication authentication) {

        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO dto = new UserProfileDTO(
                student.getId(),
                student.getEmail(),
                student.getName(),
                student.getRole(),
                student.getDepartment(),
                student.getInterests(),
                student.getYear()
        );

        return ResponseEntity.ok(dto);
    }
}
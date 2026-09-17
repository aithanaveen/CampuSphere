package com.smartcampus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Public pages & static assets
                .requestMatchers("/", "/login", "/register", "/forgot-password", "/dashboard", "/admin", "/css/**", "/js/**", "/images/**").permitAll()

                // Public API — anyone can view events (GET)
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()

                // Student self-registration & forgot-password
                .requestMatchers(HttpMethod.POST, "/api/students").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()

                // ===== ADMIN ONLY =====

                // Event management
                .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")

                // Attendance management — only ADMIN can mark attendance
                .requestMatchers(HttpMethod.POST, "/api/attendance/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/attendance/**").hasRole("ADMIN")

                // Generate AI feedback summary — only ADMIN
                .requestMatchers(HttpMethod.POST, "/api/sentiment/**").hasRole("ADMIN")

                // Student self profile update (authenticated student or admin)
                .requestMatchers(HttpMethod.PUT, "/api/students/profile").hasAnyRole("STUDENT", "ADMIN")

                // Student list (admin only)
                .requestMatchers(HttpMethod.GET, "/api/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasRole("ADMIN")

                // ===== STUDENT ONLY =====

                // Event registration (only students register)
                .requestMatchers(HttpMethod.POST, "/api/registrations/event/**").hasRole("STUDENT")

                // Withdraw (cancel) a registration (only the owning student)
                .requestMatchers(HttpMethod.DELETE, "/api/registrations/**").hasRole("STUDENT")

                // My registrations (authenticated student)
                .requestMatchers(HttpMethod.GET, "/api/registrations/my").hasAnyRole("STUDENT")

                // Feedback submission (only students submit feedback)
                .requestMatchers(HttpMethod.POST, "/api/feedback/**").hasRole("STUDENT")

                // AI recommendations (only students get recommendations)
                .requestMatchers("/api/recommendations/**").hasRole("STUDENT")

                // ===== AUTHENTICATED =====

                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/feedback", "/api/feedback/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/sentiment/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/registrations/**").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            .httpBasic(httpBasic -> {});

        return http.build();
    }
}
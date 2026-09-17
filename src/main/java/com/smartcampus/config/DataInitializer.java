package com.smartcampus.config;

import com.smartcampus.entity.Event;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.StudentRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            StudentRepository studentRepository,
            EventRepository eventRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ================================================================
            // 1. ADMIN ACCOUNT
            // ================================================================
            String adminEmail = "admin@smartcampus.com";

            if (studentRepository.findByEmail(adminEmail).isEmpty()) {

                Student admin = new Student();
                admin.setName("Campus Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setDepartment("Administration");
                admin.setYear(0);
                admin.setInterests("All");
                admin.setRole("ADMIN");

                studentRepository.save(admin);

                System.out.println("==============================================");
                System.out.println("  ADMIN ACCOUNT CREATED");
                System.out.println("  Email    : admin@smartcampus.com");
                System.out.println("  Password : admin123");
                System.out.println("  Role     : ADMIN");
                System.out.println("==============================================");
            }

            // ================================================================
            // 2. DEMO STUDENT
            // ================================================================
            String studentEmail = "student@smartcampus.com";

            if (studentRepository.findByEmail(studentEmail).isEmpty()) {

                Student student = new Student();
                student.setName("Arjun Sharma");
                student.setEmail(studentEmail);
                student.setPassword(passwordEncoder.encode("student123"));
                student.setDepartment("Computer Science");
                student.setYear(3);
                student.setInterests("AI, Java, Cloud Computing, Machine Learning");
                student.setRole("STUDENT");

                studentRepository.save(student);

                System.out.println("==============================================");
                System.out.println("  DEMO STUDENT ACCOUNT CREATED");
                System.out.println("  Email    : student@smartcampus.com");
                System.out.println("  Password : student123");
                System.out.println("  Role     : STUDENT");
                System.out.println("==============================================");
            }

            // ================================================================
            // 3. DEMO EVENTS (only if no events exist)
            // ================================================================
            if (eventRepository.count() == 0) {

                List<Event> demoEvents = List.of(

                    new Event(
                        "AI & Machine Learning Workshop",
                        "A hands-on workshop covering the fundamentals of Artificial Intelligence and Machine Learning. " +
                        "Topics include supervised learning, neural networks, and real-world AI applications. " +
                        "Ideal for CSE students interested in AI and ML.",
                        "Technology",
                        "Computer Science",
                        "2026-10-10",
                        "10:00",
                        "CSE Seminar Hall, Block A",
                        80,
                        "Department of Computer Science"
                    ),

                    new Event(
                        "Java Full Stack Development Bootcamp",
                        "An intensive 2-day bootcamp on Java Full Stack Development using Spring Boot and React. " +
                        "Students will build a complete web application from scratch. " +
                        "Java programming experience is recommended.",
                        "Technology",
                        "Computer Science",
                        "2026-10-15",
                        "09:00",
                        "Lab Complex 3, Building B",
                        60,
                        "CSE Industry Liaison Cell"
                    ),

                    new Event(
                        "Cloud Computing & DevOps Seminar",
                        "Learn about modern cloud platforms including AWS, Azure, and Google Cloud. " +
                        "Topics include containerization with Docker, Kubernetes, CI/CD pipelines, and serverless computing. " +
                        "Open to all departments.",
                        "Technology",
                        "Information Technology",
                        "2026-10-20",
                        "14:00",
                        "Auditorium, Main Block",
                        150,
                        "IT Department"
                    ),

                    new Event(
                        "National Level Hackathon 2026",
                        "24-hour national hackathon open to all students. Build innovative solutions to real-world problems. " +
                        "Categories include AI/ML, Web Development, IoT, and Sustainability. " +
                        "Top teams win cash prizes and internship opportunities.",
                        "Competition",
                        "All Departments",
                        "2026-11-01",
                        "08:00",
                        "Innovation Hub, Tech Park",
                        200,
                        "Student Technical Association"
                    ),

                    new Event(
                        "Annual Sports Meet 2026",
                        "The annual inter-department sports competition featuring Cricket, Football, Basketball, Badminton, " +
                        "and Athletics. All students are encouraged to participate and cheer for their department. " +
                        "Registration closes 3 days before the event.",
                        "Sports",
                        "All Departments",
                        "2026-11-10",
                        "07:00",
                        "College Sports Ground",
                        500,
                        "Physical Education Department"
                    ),

                    new Event(
                        "Cultural Fest — Tarang 2026",
                        "The biggest cultural celebration of the year! Featuring music, dance, drama, fine arts, " +
                        "fashion show, and celebrity performances. Open to all students. " +
                        "Showcase your talent and experience the spirit of campus culture.",
                        "Cultural",
                        "All Departments",
                        "2026-11-20",
                        "17:00",
                        "Open Air Theatre, Central Campus",
                        1000,
                        "Cultural Committee"
                    )
                );

                eventRepository.saveAll(demoEvents);

                System.out.println("==============================================");
                System.out.println("  " + demoEvents.size() + " DEMO EVENTS CREATED");
                System.out.println("==============================================");
            }
        };
    }
}
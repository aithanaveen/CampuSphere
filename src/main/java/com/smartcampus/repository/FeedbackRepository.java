package com.smartcampus.repository;

import com.smartcampus.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByStudentId(Long studentId);

    List<Feedback> findByEventId(Long eventId);

    long countByEventId(Long eventId);

    long countByEventIdAndSentiment(Long eventId, String sentiment);
}
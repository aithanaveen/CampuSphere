package com.smartcampus.repository;

import com.smartcampus.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {

    List<Registration> findByStudentId(Long studentId);

    List<Registration> findByEventId(Long eventId);

    boolean existsByStudentIdAndEventId(Long studentId, Long eventId);

    long countByEventId(Long eventId);
}
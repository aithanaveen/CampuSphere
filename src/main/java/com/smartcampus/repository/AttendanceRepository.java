package com.smartcampus.repository;

import com.smartcampus.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByRegistrationId(Long registrationId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.registration.student.id = :studentId AND a.attended = true")
    long countAttendedByStudentId(@Param("studentId") Long studentId);
}
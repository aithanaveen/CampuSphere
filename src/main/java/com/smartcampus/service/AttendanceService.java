package com.smartcampus.service;

import com.smartcampus.entity.Attendance;
import com.smartcampus.entity.Registration;
import com.smartcampus.repository.AttendanceRepository;
import com.smartcampus.repository.RegistrationRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final RegistrationRepository registrationRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            RegistrationRepository registrationRepository) {

        this.attendanceRepository = attendanceRepository;
        this.registrationRepository = registrationRepository;
    }

    public Attendance markAttendance(
            Long registrationId,
            boolean attended) {

        Registration registration =
                registrationRepository.findById(registrationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Registration not found"));

        Attendance attendance =
                attendanceRepository
                .findByRegistrationId(registrationId)
                .orElse(new Attendance());

        attendance.setRegistration(registration);
        attendance.setAttended(attended);
        attendance.setMarkedAt(LocalDateTime.now());

        return attendanceRepository.save(attendance);
    }

    public Attendance getAttendance(Long registrationId) {

        return attendanceRepository
                .findByRegistrationId(registrationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Attendance not found"));
    }
}
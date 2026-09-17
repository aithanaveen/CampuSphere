package com.smartcampus.controller;

import com.smartcampus.entity.Attendance;
import com.smartcampus.service.AttendanceService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

/**
 * Attendance management controller.
 * Only ADMIN can mark or view attendance.
 * Students cannot mark their own attendance.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Admin marks attendance for a registration.
     * POST /api/attendance/registration/{registrationId}?attended=true
     */
    @PostMapping("/registration/{registrationId}")
    public ResponseEntity<Attendance> markAttendance(
            @PathVariable Long registrationId,
            @RequestParam boolean attended) {

        Attendance attendance = attendanceService.markAttendance(registrationId, attended);
        return ResponseEntity.ok(attendance);
    }

    /**
     * Admin views attendance for a specific registration.
     * GET /api/attendance/registration/{registrationId}
     */
    @GetMapping("/registration/{registrationId}")
    public ResponseEntity<Attendance> getAttendance(
            @PathVariable Long registrationId) {

        Attendance attendance = attendanceService.getAttendance(registrationId);
        return ResponseEntity.ok(attendance);
    }
}
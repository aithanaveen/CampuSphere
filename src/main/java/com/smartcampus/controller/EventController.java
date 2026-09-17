package com.smartcampus.controller;

import com.smartcampus.entity.Event;
import com.smartcampus.repository.RegistrationRepository;
import com.smartcampus.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationRepository registrationRepository;

    public EventController(EventService eventService, RegistrationRepository registrationRepository) {
        this.eventService = eventService;
        this.registrationRepository = registrationRepository;
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {

        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @RequestBody Event event) {

        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {

        eventService.deleteEvent(id);

        return ResponseEntity.ok("Event deleted successfully");
    }

    /**
     * Change the lifecycle status of an event.
     * Body: { "status": "UPCOMING" | "ONGOING" | "COMPLETED" | "CANCELLED" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Event> updateEventStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Event updated = eventService.updateEventStatus(id, status.toUpperCase());
        return ResponseEntity.ok(updated);
    }

    /**
     * Get registration count for an event (used for capacity display).
     */
    @GetMapping("/{id}/registration-count")
    public ResponseEntity<Long> getRegistrationCount(@PathVariable Long id) {
        long count = registrationRepository.countByEventId(id);
        return ResponseEntity.ok(count);
    }
}
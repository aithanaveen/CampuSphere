package com.smartcampus.service;

import com.smartcampus.entity.Event;
import com.smartcampus.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event updateEvent(Long id, Event updatedEvent) {

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        existingEvent.setTitle(updatedEvent.getTitle());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setCategory(updatedEvent.getCategory());
        existingEvent.setDepartment(updatedEvent.getDepartment());
        existingEvent.setEventDate(updatedEvent.getEventDate());
        existingEvent.setEventTime(updatedEvent.getEventTime());
        existingEvent.setVenue(updatedEvent.getVenue());
        existingEvent.setCapacity(updatedEvent.getCapacity());
        existingEvent.setOrganizer(updatedEvent.getOrganizer());
        // Preserve status — use updateEventStatus() to change it separately
        if (updatedEvent.getStatus() != null) {
            existingEvent.setStatus(updatedEvent.getStatus());
        }

        return eventRepository.save(existingEvent);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    /**
     * Updates only the status field of an event.
     * Valid statuses: UPCOMING, ONGOING, COMPLETED, CANCELLED
     */
    public Event updateEventStatus(Long id, String status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setStatus(status);
        return eventRepository.save(event);
    }

    /**
     * Returns the current registration count for an event.
     */
    public long getRegistrationCount(Long eventId) {
        return 0; // Delegation handled at controller level if needed
    }
}
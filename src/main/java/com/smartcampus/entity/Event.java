package com.smartcampus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String category;

    private String department;

    private String eventDate;

    private String eventTime;

    private String venue;

    private Integer capacity;

    private String organizer;

    /** Event lifecycle status: UPCOMING | ONGOING | COMPLETED | CANCELLED */
    private String status = "UPCOMING";

    @Column(length = 3000)
    private String aiFeedbackSummary;

    // Constructors

    public Event() {
    }

    public Event(String title, String description, String category,
                 String department, String eventDate, String eventTime,
                 String venue, Integer capacity, String organizer) {

        this.title = title;
        this.description = description;
        this.category = category;
        this.department = department;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.capacity = capacity;
        this.organizer = organizer;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getAiFeedbackSummary() {
        return aiFeedbackSummary;
    }

    public void setAiFeedbackSummary(String aiFeedbackSummary) {
        this.aiFeedbackSummary = aiFeedbackSummary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
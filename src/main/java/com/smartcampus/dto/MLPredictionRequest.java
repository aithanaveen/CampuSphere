package com.smartcampus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MLPredictionRequest {

    @JsonProperty("student_department")
    private String studentDepartment;

    @JsonProperty("student_year")
    private Integer studentYear;

    @JsonProperty("event_category")
    private String eventCategory;

    @JsonProperty("event_department")
    private String eventDepartment;

    @JsonProperty("previous_registrations")
    private Integer previousRegistrations;

    @JsonProperty("previous_attendance")
    private Integer previousAttendance;

    @JsonProperty("category_match")
    private Integer categoryMatch;

    @JsonProperty("department_match")
    private Integer departmentMatch;

    @JsonProperty("event_popularity")
    private Integer eventPopularity;

    @JsonProperty("event_id")
    private Long eventId;

    public MLPredictionRequest() {
    }

    public MLPredictionRequest(
            String studentDepartment,
            Integer studentYear,
            String eventCategory,
            String eventDepartment,
            Integer previousRegistrations,
            Integer previousAttendance,
            Integer categoryMatch,
            Integer departmentMatch,
            Integer eventPopularity,
            Long eventId) {

        this.studentDepartment = studentDepartment;
        this.studentYear = studentYear;
        this.eventCategory = eventCategory;
        this.eventDepartment = eventDepartment;
        this.previousRegistrations = previousRegistrations;
        this.previousAttendance = previousAttendance;
        this.categoryMatch = categoryMatch;
        this.departmentMatch = departmentMatch;
        this.eventPopularity = eventPopularity;
        this.eventId = eventId;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public Integer getStudentYear() {
        return studentYear;
    }

    public void setStudentYear(Integer studentYear) {
        this.studentYear = studentYear;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventDepartment() {
        return eventDepartment;
    }

    public void setEventDepartment(String eventDepartment) {
        this.eventDepartment = eventDepartment;
    }

    public Integer getPreviousRegistrations() {
        return previousRegistrations;
    }

    public void setPreviousRegistrations(Integer previousRegistrations) {
        this.previousRegistrations = previousRegistrations;
    }

    public Integer getPreviousAttendance() {
        return previousAttendance;
    }

    public void setPreviousAttendance(Integer previousAttendance) {
        this.previousAttendance = previousAttendance;
    }

    public Integer getCategoryMatch() {
        return categoryMatch;
    }

    public void setCategoryMatch(Integer categoryMatch) {
        this.categoryMatch = categoryMatch;
    }

    public Integer getDepartmentMatch() {
        return departmentMatch;
    }

    public void setDepartmentMatch(Integer departmentMatch) {
        this.departmentMatch = departmentMatch;
    }

    public Integer getEventPopularity() {
        return eventPopularity;
    }

    public void setEventPopularity(Integer eventPopularity) {
        this.eventPopularity = eventPopularity;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}

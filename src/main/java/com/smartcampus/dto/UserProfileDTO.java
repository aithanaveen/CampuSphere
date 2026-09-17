package com.smartcampus.dto;

/**
 * DTO returned by /api/auth/me.
 * Contains the authenticated user's public profile information.
 * Used by the frontend to determine role-based routing (ADMIN or STUDENT).
 */
public class UserProfileDTO {

    private Long id;
    private String email;
    private String name;
    private String role;
    private String department;
    private String interests;
    private Integer year;

    public UserProfileDTO() {
    }

    public UserProfileDTO(Long id, String email, String name, String role,
                          String department, String interests, Integer year) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.department = department;
        this.interests = interests;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

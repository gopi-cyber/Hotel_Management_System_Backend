package com.luxestay.hotel.dto;

public class UserResponse {
    private String id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String department;
    private String shift;
    private String employeeId;
    private String avatarUrl;

    public UserResponse() {}

    public UserResponse(Long id, String username, String name, String email, String phone, String role,
                        String department, String shift, String employeeId, String avatarUrl) {
        this.id = id != null ? String.valueOf(id) : null;
        this.username = username;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.department = department;
        this.shift = shift;
        this.employeeId = employeeId;
        this.avatarUrl = avatarUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

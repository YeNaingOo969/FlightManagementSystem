package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String email;
    private String password;
    private String role;
    private String status;
    private String imagePath;
    private LocalDateTime createdAt;

    public User() {}

    public User(int userId, String username, String email, String password, String role, String status, String imagePath, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public User(int userId, String username, String password, String email, String role, String imagePath, LocalDateTime createdAt) {
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
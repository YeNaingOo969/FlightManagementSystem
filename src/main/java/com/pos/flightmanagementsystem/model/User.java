package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private int role;

    // No-argument Constructor
    public User() {
    }

    // All-argument Constructor
    public User(int userId, String username, String password, LocalDateTime createdAt, int role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    // toString Method
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", createdAt=" + createdAt +
                ", role=" + role +
                '}';
    }
}
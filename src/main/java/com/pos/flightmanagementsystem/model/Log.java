package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class Log {
    private int logId;
    private int userId;
    private LocalDateTime timestamp;

    // No-argument Constructor
    public Log() {
    }

    // All-argument Constructor
    public Log(int logId, int userId, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // toString Method
    @Override
    public String toString() {
        return "Log{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", timestamp=" + timestamp +
                '}';
    }
}
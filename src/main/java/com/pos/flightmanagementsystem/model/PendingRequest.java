package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class PendingRequest {
    private int requestId;
    private int bookingId;
    private Integer staffId;
    private Integer adminId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    // No-argument Constructor
    public PendingRequest() {
    }

    // All-argument Constructor
    public PendingRequest(int requestId, int bookingId, Integer staffId, Integer adminId, String reason, String status, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.bookingId = bookingId;
        this.staffId = staffId;
        this.adminId = adminId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // toString Method
    @Override
    public String toString() {
        return "PendingRequest{" +
                "requestId=" + requestId +
                ", bookingId=" + bookingId +
                ", staffId=" + staffId +
                ", adminId=" + adminId +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
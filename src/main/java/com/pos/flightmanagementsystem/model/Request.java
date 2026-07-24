package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class Request {
    private int requestId;
    private int bookingId;
    private int staffId;
    private int adminId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public Request() {}

    public Request(int requestId, int bookingId, int staffId, int adminId, String reason, String status, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.bookingId = bookingId;
        this.staffId = staffId;
        this.adminId = adminId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
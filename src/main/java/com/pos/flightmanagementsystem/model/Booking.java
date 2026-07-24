package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private String pnrCode;
    private int flightId;
    private int passengerId;
    private int seatId;
    private int staffId;
    private double totalPrice;
    private String paymentMethod;
    private String status;
    private LocalDateTime bookingDate;

    public Booking() {}

    public Booking(int bookingId, String pnrCode, int flightId, int passengerId, int seatId, int staffId, double totalPrice, String paymentMethod, String status, LocalDateTime bookingDate) {
        this.bookingId = bookingId;
        this.pnrCode = pnrCode;
        this.flightId = flightId;
        this.passengerId = passengerId;
        this.seatId = seatId;
        this.staffId = staffId;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.bookingDate = bookingDate;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getPnrCode() { return pnrCode; }
    public void setPnrCode(String pnrCode) { this.pnrCode = pnrCode; }

    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
}
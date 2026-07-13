package com.pos.flightmanagementsystem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int flightId;
    private String passengerName;
    private String seatNumber;
    private LocalDateTime bookingDate;
    private String pnrCode;
    private Integer staffId;
    private int seatId;
    private BigDecimal price;
    private String paymentMethod;
    private String nrcNo;
    private String status;

    // No-argument Constructor
    public Booking() {
    }

    // All-argument Constructor
    public Booking(int bookingId, int flightId, String passengerName, String seatNumber, LocalDateTime bookingDate, String pnrCode, Integer staffId, int seatId, BigDecimal price, String paymentMethod, String nrcNo, String status) {
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
        this.pnrCode = pnrCode;
        this.staffId = staffId;
        this.seatId = seatId;
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.nrcNo = nrcNo;
        this.status = status;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getPnrCode() {
        return pnrCode;
    }

    public void setPnrCode(String pnrCode) {
        this.pnrCode = pnrCode;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNrcNo() {
        return nrcNo;
    }

    public void setNrcNo(String nrcNo) {
        this.nrcNo = nrcNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString Method
    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", flightId=" + flightId +
                ", passengerName='" + passengerName + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", bookingDate=" + bookingDate +
                ", pnrCode='" + pnrCode + '\'' +
                ", staffId=" + staffId +
                ", seatId=" + seatId +
                ", price=" + price +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", nrcNo='" + nrcNo + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
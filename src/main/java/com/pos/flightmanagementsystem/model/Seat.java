package com.pos.flightmanagementsystem.model;

public class Seat {
    private int seatId;
    private int flightId;
    private String airline;
    private String seatNumber;
    private boolean isAvailable;

    // No-argument Constructor
    public Seat() {
    }

    // All-argument Constructor
    public Seat(int seatId, int flightId, String airline, String seatNumber, boolean isAvailable) {
        this.seatId = seatId;
        this.flightId = flightId;
        this.airline = airline;
        this.seatNumber = seatNumber;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // toString Method
    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", flightId=" + flightId +
                ", airline='" + airline + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
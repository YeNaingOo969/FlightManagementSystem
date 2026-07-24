package com.pos.flightmanagementsystem.model;

public class Seat {
    private int seatId;
    private int flightId;
    private String seatNumber;
    private String seatClass;
    private boolean isAvailable;

    public Seat() {}

    public Seat(int seatId, int flightId, String seatNumber, String seatClass, boolean isAvailable) {
        this.seatId = seatId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.isAvailable = isAvailable;
    }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }

    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
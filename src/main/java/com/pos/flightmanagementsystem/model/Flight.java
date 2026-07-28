package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class Flight {
    private int flightId;
    private String flightNumber;
    private String airline;
    private int departureCityId;
    private int arrivalCityId;
    // အသစ်ထည့်ထားသော City Name များ
    private String departureCityName;
    private String arrivalCityName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int totalSeats;
    private double basePrice;
    private String status;

    public Flight() {}

    public Flight(int flightId, String flightNumber, String airline, String departureCityName, String arrivalCityName, LocalDateTime departureTime, LocalDateTime arrivalTime, int totalSeats, double basePrice, String status) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.departureCityName = departureCityName;
        this.arrivalCityName = arrivalCityName;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.basePrice = basePrice;
        this.status = status;
    }

    // Getters and Setters
    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public int getDepartureCityId() { return departureCityId; }
    public void setDepartureCityId(int departureCityId) { this.departureCityId = departureCityId; }

    public int getArrivalCityId() { return arrivalCityId; }
    public void setArrivalCityId(int arrivalCityId) { this.arrivalCityId = arrivalCityId; }

    public String getDepartureCityName() { return departureCityName; }
    public void setDepartureCityName(String departureCityName) { this.departureCityName = departureCityName; }

    public String getArrivalCityName() { return arrivalCityName; }
    public void setArrivalCityName(String arrivalCityName) { this.arrivalCityName = arrivalCityName; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
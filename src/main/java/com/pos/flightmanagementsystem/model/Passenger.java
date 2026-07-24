package com.pos.flightmanagementsystem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Passenger {
    private int passengerId;
    private String fullName;
    private String nrcPassportNo;
    private String phone;
    private String email;
    private String gender;
    private LocalDate dob;
    private String nationality;
    private LocalDateTime createdAt;

    public Passenger() {}

    public Passenger(int passengerId, String fullName, String nrcPassportNo, String phone, String email, String gender, LocalDate dob, String nationality, LocalDateTime createdAt) {
        this.passengerId = passengerId;
        this.fullName = fullName;
        this.nrcPassportNo = nrcPassportNo;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.dob = dob;
        this.nationality = nationality;
        this.createdAt = createdAt;
    }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNrcPassportNo() { return nrcPassportNo; }
    public void setNrcPassportNo(String nrcPassportNo) { this.nrcPassportNo = nrcPassportNo; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
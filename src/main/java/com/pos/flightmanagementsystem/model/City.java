package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class City {
    private int cityId;
    private String cityCode;
    private String cityName;
    private String country;
    private LocalDateTime createdAt;

    public City() {}

    public City(int cityId, String cityCode, String cityName, String country, LocalDateTime createdAt) {
        this.cityId = cityId;
        this.cityCode = cityCode;
        this.cityName = cityName;
        this.country = country;
        this.createdAt = createdAt;
    }

    public int getCityId() { return cityId; }
    public void setCityId(int cityId) { this.cityId = cityId; }

    public String getCityCode() { return cityCode; }
    public void setCityCode(String cityCode) { this.cityCode = cityCode; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
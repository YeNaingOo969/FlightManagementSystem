package com.pos.flightmanagementsystem.model;

public class BackupHistory {
    private String date;
    private String fileName;
    private String size;
    private String action;

    // Constructor
    public BackupHistory(String date, String fileName, String size, String action) {
        this.date = date;
        this.fileName = fileName;
        this.size = size;
        this.action = action;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public String getFileName() {
        return fileName;
    }

    public String getSize() {
        return size;
    }

    public String getAction() {
        return action;
    }

    // Setters
    public void setDate(String date) {
        this.date = date;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
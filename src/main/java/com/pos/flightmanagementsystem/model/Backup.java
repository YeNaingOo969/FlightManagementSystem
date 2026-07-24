package com.pos.flightmanagementsystem.model;

import java.time.LocalDateTime;

public class Backup {
    private int id;
    private String fileName;
    private String fileSize;
    private String actionType;
    private LocalDateTime backupDate;

    public Backup() {}

    public Backup(int id, String fileName, String fileSize, String actionType, LocalDateTime backupDate) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.actionType = actionType;
        this.backupDate = backupDate;
    }

    public Backup(String date, String fileName, String size, String action) {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public LocalDateTime getBackupDate() { return backupDate; }
    public void setBackupDate(LocalDateTime backupDate) { this.backupDate = backupDate; }
}
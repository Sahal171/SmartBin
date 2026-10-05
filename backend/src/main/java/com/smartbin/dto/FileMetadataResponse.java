package com.smartbin.dto;

import java.time.Instant;

public class FileMetadataResponse {
    private String transferCode;
    private String transferType = "FILE"; // "FILE" or "TEXT"
    private String textContent;
    private String filename;
    private Long fileSize;
    private String contentType;
    private Instant createdAt;
    private Instant expiresAt;
    private long remainingSeconds;

    public FileMetadataResponse() {
    }

    public FileMetadataResponse(String transferCode, String transferType, String textContent,
                                String filename, Long fileSize,
                                String contentType, Instant createdAt, Instant expiresAt,
                                long remainingSeconds) {
        this.transferCode = transferCode;
        this.transferType = transferType != null ? transferType : "FILE";
        this.textContent = textContent;
        this.filename = filename;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.remainingSeconds = remainingSeconds;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}

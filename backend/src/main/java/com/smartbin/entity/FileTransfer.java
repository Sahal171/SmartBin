package com.smartbin.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_transfers", indexes = {
        @Index(name = "idx_transfer_code", columnList = "transfer_code", unique = true),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
public class FileTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transfer_code", length = 8, nullable = false, unique = true)
    private String transferCode;

    @Column(name = "transfer_type", length = 16, nullable = false)
    private String transferType = "FILE"; // "FILE" or "TEXT"

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "original_filename", length = 255, nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", length = 255, nullable = false, unique = true)
    private String storedFilename;

    @Column(name = "file_path", length = 512, nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 128, nullable = false)
    private String contentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public FileTransfer() {
    }

    public FileTransfer(String transferCode, String transferType, String textContent,
                        String originalFilename, String storedFilename,
                        String filePath, Long fileSize, String contentType,
                        Instant createdAt, Instant expiresAt) {
        this.transferCode = transferCode;
        this.transferType = transferType != null ? transferType : "FILE";
        this.textContent = textContent;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (transferType == null) {
            transferType = "FILE";
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}

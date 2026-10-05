package com.smartbin.service;

import com.smartbin.config.AppProperties;
import com.smartbin.dto.FileMetadataResponse;
import com.smartbin.dto.FileUploadResponse;
import com.smartbin.dto.TextMessageRequest;
import com.smartbin.entity.FileTransfer;
import com.smartbin.exception.FileStorageException;
import com.smartbin.exception.ResourceNotFoundException;
import com.smartbin.repository.FileTransferRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileTransferService {

    private final FileTransferRepository repository;
    private final StorageService storageService;
    private final TransferCodeService codeService;
    private final AppProperties appProperties;

    public FileTransferService(FileTransferRepository repository,
                               StorageService storageService,
                               TransferCodeService codeService,
                               AppProperties appProperties) {
        this.repository = repository;
        this.storageService = storageService;
        this.codeService = codeService;
        this.appProperties = appProperties;
    }

    /**
     * Handles file upload, saves physical file, and creates database record.
     */
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot upload an empty file.");
        }

        // Sanitize original filename
        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.isBlank()) {
            rawFilename = "unnamed_file";
        }
        String originalFilename = new File(rawFilename).getName(); // strip any directory components
        originalFilename = StringUtils.cleanPath(originalFilename);
        if (originalFilename.contains("..") || originalFilename.isBlank()) {
            originalFilename = "file_" + System.currentTimeMillis();
        }

        // Generate safe stored filename
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
            String ext = originalFilename.substring(dotIndex).replaceAll("[^a-zA-Z0-9.]", "");
            if (ext.length() <= 10) {
                extension = ext;
            }
        }
        String storedFilename = UUID.randomUUID() + extension;

        // Store physical file
        String physicalPath = storageService.storeFile(file, storedFilename);

        // Determine content type safely
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        // Generate cryptographically secure transfer code
        String transferCode = codeService.generateUniqueCode();

        // Calculate expiration time
        int expirationMinutes = appProperties.getFile().getDefaultExpirationMinutes();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(expirationMinutes));

        FileTransfer entity = new FileTransfer(
                transferCode,
                "FILE",
                null,
                originalFilename,
                storedFilename,
                physicalPath,
                file.getSize(),
                contentType,
                now,
                expiresAt
        );

        repository.save(entity);

        long remainingSeconds = Duration.between(now, expiresAt).getSeconds();

        return new FileUploadResponse(
                transferCode,
                "FILE",
                null,
                originalFilename,
                file.getSize(),
                contentType,
                now,
                expiresAt,
                remainingSeconds,
                "File uploaded successfully. Use the transfer code to retrieve your file."
        );
    }

    /**
     * Handles text note / snippet sharing.
     */
    @Transactional
    public FileUploadResponse uploadText(TextMessageRequest request) {
        if (request == null || request.getText() == null || request.getText().isBlank()) {
            throw new FileStorageException("Message text cannot be empty.");
        }

        String rawTitle = request.getTitle();
        String originalFilename;
        if (rawTitle != null && !rawTitle.isBlank()) {
            originalFilename = StringUtils.cleanPath(new File(rawTitle.trim()).getName());
            if (!originalFilename.endsWith(".txt")) {
                originalFilename = originalFilename + ".txt";
            }
        } else {
            originalFilename = "note-" + System.currentTimeMillis() + ".txt";
        }

        String storedFilename = UUID.randomUUID() + ".txt";
        byte[] bytes = request.getText().getBytes(StandardCharsets.UTF_8);

        // Store as physical file for streaming download capability
        String physicalPath = storageService.storeFile(new ByteArrayInputStream(bytes), storedFilename);

        String transferCode = codeService.generateUniqueCode();
        int expirationMinutes = appProperties.getFile().getDefaultExpirationMinutes();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(expirationMinutes));

        FileTransfer entity = new FileTransfer(
                transferCode,
                "TEXT",
                request.getText(),
                originalFilename,
                storedFilename,
                physicalPath,
                (long) bytes.length,
                "text/plain;charset=UTF-8",
                now,
                expiresAt
        );

        repository.save(entity);

        long remainingSeconds = Duration.between(now, expiresAt).getSeconds();

        return new FileUploadResponse(
                transferCode,
                "TEXT",
                request.getText(),
                originalFilename,
                (long) bytes.length,
                "text/plain;charset=UTF-8",
                now,
                expiresAt,
                remainingSeconds,
                "Text message shared successfully. Use the transfer code to retrieve your message."
        );
    }

    /**
     * Retrieves file metadata by transfer code.
     */
    @Transactional(readOnly = true)
    public FileMetadataResponse getFileMetadata(String code) {
        FileTransfer transfer = getActiveTransfer(code);
        long remainingSeconds = Math.max(0, Duration.between(Instant.now(), transfer.getExpiresAt()).getSeconds());

        return new FileMetadataResponse(
                transfer.getTransferCode(),
                transfer.getTransferType(),
                transfer.getTextContent(),
                transfer.getOriginalFilename(),
                transfer.getFileSize(),
                transfer.getContentType(),
                transfer.getCreatedAt(),
                transfer.getExpiresAt(),
                remainingSeconds
        );
    }

    /**
     * Resolves the stored resource for streaming download.
     */
    @Transactional(readOnly = true)
    public DownloadData prepareDownload(String code) {
        FileTransfer transfer = getActiveTransfer(code);
        Resource resource = storageService.loadAsResource(transfer.getStoredFilename());

        return new DownloadData(
                resource,
                transfer.getOriginalFilename(),
                transfer.getContentType(),
                transfer.getFileSize()
        );
    }

    /**
     * Deletes a file transfer physically and logically.
     */
    @Transactional
    public void deleteByCode(String code) {
        String cleanCode = normalizeCode(code);
        FileTransfer transfer = repository.findByTransferCode(cleanCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired transfer code."));

        // Delete from physical storage
        storageService.delete(transfer.getStoredFilename());

        // Delete from database
        repository.delete(transfer);
    }

    /**
     * Cleans up all expired files from storage and database.
     */
    @Transactional
    public int cleanupExpiredFiles() {
        Instant now = Instant.now();
        List<FileTransfer> expiredTransfers = repository.findAllByExpiresAtBefore(now);

        for (FileTransfer transfer : expiredTransfers) {
            try {
                storageService.delete(transfer.getStoredFilename());
            } catch (Exception ignored) {
            }
            repository.delete(transfer);
        }

        return expiredTransfers.size();
    }

    private FileTransfer getActiveTransfer(String code) {
        String cleanCode = normalizeCode(code);
        FileTransfer transfer = repository.findByTransferCode(cleanCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired transfer code."));

        if (transfer.isExpired()) {
            // Delete expired file on access
            storageService.delete(transfer.getStoredFilename());
            repository.delete(transfer);
            throw new ResourceNotFoundException("Invalid or expired transfer code.");
        }

        return transfer;
    }

    private String normalizeCode(String code) {
        if (code == null) {
            throw new ResourceNotFoundException("Invalid or expired transfer code.");
        }
        return code.trim().toUpperCase();
    }

    public record DownloadData(Resource resource, String originalFilename, String contentType, Long fileSize) {
    }
}

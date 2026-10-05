package com.smartbin.controller;

import com.smartbin.dto.FileMetadataResponse;
import com.smartbin.dto.FileUploadResponse;
import com.smartbin.dto.RetrieveRequest;
import com.smartbin.dto.TextMessageRequest;
import com.smartbin.service.FileTransferService;
import com.smartbin.service.RateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileTransferController {

    private final FileTransferService fileTransferService;
    private final RateLimitingService rateLimitingService;

    public FileTransferController(FileTransferService fileTransferService,
                                  RateLimitingService rateLimitingService) {
        this.fileTransferService = fileTransferService;
        this.rateLimitingService = rateLimitingService;
    }

    /**
     * Uploads a file, returns transfer code and expiration info.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileTransferService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Shares a text note / snippet, returns transfer code and expiration info.
     */
    @PostMapping("/text")
    public ResponseEntity<FileUploadResponse> uploadText(@Valid @RequestBody TextMessageRequest request) {
        FileUploadResponse response = fileTransferService.uploadText(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves file metadata via POST /api/files/retrieve (with rate-limiting protection).
     */
    @PostMapping("/retrieve")
    public ResponseEntity<FileMetadataResponse> retrieveFile(@Valid @RequestBody RetrieveRequest request,
                                                             HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        rateLimitingService.checkRateLimit(clientIp);

        FileMetadataResponse response = fileTransferService.getFileMetadata(request.getTransferCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets file metadata by transfer code via GET /api/files/{code} (with rate-limiting protection).
     */
    @GetMapping("/{code}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(@PathVariable("code") String code,
                                                               HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        rateLimitingService.checkRateLimit(clientIp);

        FileMetadataResponse response = fileTransferService.getFileMetadata(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Streams file download without loading the whole file into server memory.
     */
    @GetMapping("/{code}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("code") String code) {
        FileTransferService.DownloadData download = fileTransferService.prepareDownload(code);

        String encodedFilename = URLEncoder.encode(download.originalFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(download.contentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(download.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.originalFilename() + "\"; filename*=UTF-8''" + encodedFilename)
                .body(download.resource());
    }

    /**
     * Manually deletes an uploaded file before its expiration time.
     */
    @DeleteMapping("/{code}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable("code") String code) {
        fileTransferService.deleteByCode(code);
        return ResponseEntity.ok(Map.of(
                "message", "File successfully deleted and transfer code invalidated."
        ));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

package com.smartbin.service.impl;

import com.smartbin.config.AppProperties;
import com.smartbin.exception.FileNotFoundException;
import com.smartbin.exception.FileStorageException;
import com.smartbin.service.StorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(AppProperties appProperties) {
        String location = appProperties.getStorage().getLocation();
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage directory: " + rootLocation, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String storedFilename) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            return storeFile(inputStream, storedFilename);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read uploaded file.", e);
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String storedFilename) {
        try {
            String cleanName = StringUtils.cleanPath(Objects.requireNonNull(storedFilename));
            if (cleanName.contains("..")) {
                throw new FileStorageException("Cannot store file with relative path outside directory: " + cleanName);
            }

            Path destinationFile = this.rootLocation.resolve(cleanName).normalize().toAbsolutePath();
            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new FileStorageException("Cannot store file outside current storage directory.");
            }

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return destinationFile.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + storedFilename, e);
        }
    }

    @Override
    public Path load(String storedFilename) {
        return rootLocation.resolve(storedFilename).normalize();
    }

    @Override
    public Resource loadAsResource(String storedFilename) {
        try {
            Path file = load(storedFilename);
            if (!file.toAbsolutePath().startsWith(rootLocation)) {
                throw new FileStorageException("Access denied to file outside storage directory.");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found or unreadable: " + storedFilename);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Invalid file URL for: " + storedFilename, e);
        }
    }

    @Override
    public boolean delete(String storedFilename) {
        try {
            Path file = load(storedFilename);
            if (file.toAbsolutePath().startsWith(rootLocation)) {
                return Files.deleteIfExists(file);
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean exists(String storedFilename) {
        Path file = load(storedFilename);
        return Files.exists(file);
    }
}

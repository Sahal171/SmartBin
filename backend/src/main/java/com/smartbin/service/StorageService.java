package com.smartbin.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

public interface StorageService {

    void init();

    String storeFile(MultipartFile file, String storedFilename);

    String storeFile(InputStream inputStream, String storedFilename);

    Resource loadAsResource(String storedFilename);

    Path load(String storedFilename);

    boolean delete(String storedFilename);

    boolean exists(String storedFilename);
}

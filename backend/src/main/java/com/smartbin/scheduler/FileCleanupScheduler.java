package com.smartbin.scheduler;

import com.smartbin.service.FileTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FileCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupScheduler.class);

    private final FileTransferService fileTransferService;

    public FileCleanupScheduler(FileTransferService fileTransferService) {
        this.fileTransferService = fileTransferService;
    }

    /**
     * Periodically runs every minute to remove expired files from physical disk and database.
     */
    @Scheduled(cron = "${smartbin.file.cleanup-cron:0 * * * * *}")
    public void cleanupExpiredFiles() {
        try {
            int cleanedCount = fileTransferService.cleanupExpiredFiles();
            if (cleanedCount > 0) {
                log.info("SmartBin Cleanup: Purged {} expired file(s).", cleanedCount);
            }
        } catch (Exception e) {
            log.error("SmartBin Cleanup error during expired files sweep: {}", e.getMessage(), e);
        }
    }
}

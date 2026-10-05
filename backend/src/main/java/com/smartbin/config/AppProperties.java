package com.smartbin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "smartbin")
public class AppProperties {

    private Storage storage = new Storage();
    private FileConfig file = new FileConfig();
    private RateLimit ratelimit = new RateLimit();
    private Cors cors = new Cors();

    public static class Storage {
        private String location = "./uploads";

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class FileConfig {
        private int defaultExpirationMinutes = 30;
        private String cleanupCron = "0 * * * * *";

        public int getDefaultExpirationMinutes() {
            return defaultExpirationMinutes;
        }

        public void setDefaultExpirationMinutes(int defaultExpirationMinutes) {
            this.defaultExpirationMinutes = defaultExpirationMinutes;
        }

        public String getCleanupCron() {
            return cleanupCron;
        }

        public void setCleanupCron(String cleanupCron) {
            this.cleanupCron = cleanupCron;
        }
    }

    public static class RateLimit {
        private int maxAttempts = 10;
        private int windowSeconds = 60;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000");

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public FileConfig getFile() {
        return file;
    }

    public void setFile(FileConfig file) {
        this.file = file;
    }

    public RateLimit getRatelimit() {
        return ratelimit;
    }

    public void setRatelimit(RateLimit ratelimit) {
        this.ratelimit = ratelimit;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }
}

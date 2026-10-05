package com.smartbin.service;

import com.smartbin.config.AppProperties;
import com.smartbin.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final AppProperties appProperties;
    private final ConcurrentHashMap<String, List<Instant>> ipAttempts = new ConcurrentHashMap<>();

    public RateLimitingService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Checks if the given IP address has exceeded the rate limit.
     * Throws RateLimitExceededException if limit is exceeded.
     */
    public void checkRateLimit(String clientIp) {
        int maxAttempts = appProperties.getRatelimit().getMaxAttempts();
        int windowSeconds = appProperties.getRatelimit().getWindowSeconds();
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(windowSeconds);

        List<Instant> attempts = ipAttempts.computeIfAbsent(clientIp, k -> new LinkedList<>());

        synchronized (attempts) {
            // Remove attempts older than the window
            Iterator<Instant> iterator = attempts.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().isBefore(cutoff)) {
                    iterator.remove();
                } else {
                    break;
                }
            }

            if (attempts.size() >= maxAttempts) {
                throw new RateLimitExceededException(
                        "Too many retrieval attempts. Please wait " + windowSeconds + " seconds before trying again."
                );
            }

            attempts.add(now);
        }
    }

    /**
     * Clears tracked attempts for an IP (e.g. on successful retrieval).
     */
    public void resetAttempts(String clientIp) {
        ipAttempts.remove(clientIp);
    }
}

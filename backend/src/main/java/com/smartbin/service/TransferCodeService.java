package com.smartbin.service;

import com.smartbin.repository.FileTransferRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class TransferCodeService {

    // 32-character unambiguous charset (omitting 0, O, 1, I)
    private static final String CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom secureRandom = new SecureRandom();
    private final FileTransferRepository repository;

    public TransferCodeService(FileTransferRepository repository) {
        this.repository = repository;
    }

    /**
     * Generates a cryptographically secure, unique 6-character transfer code.
     */
    public String generateUniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = generateRandomCode();
            if (!repository.existsByTransferCode(code)) {
                return code;
            }
        }
        // Extremely rare fallback if collisions occur
        return generateRandomCode() + (char) ('A' + secureRandom.nextInt(26));
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        return sb.toString();
    }
}

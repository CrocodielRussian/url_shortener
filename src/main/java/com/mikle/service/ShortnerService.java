package com.mikle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ShortnerService {

    private static final int CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 8; // максимум сдвигов по хешу

    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final BloomFilterService bloomFilter;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateShortUrl(String longUrl) {
        String hash = sha256(longUrl);

        for (int i = 0; i <= MAX_ATTEMPTS; i++) {
            int start = (i * CODE_LENGTH) % (hash.length() - CODE_LENGTH);
            String code = hash.substring(start, start + CODE_LENGTH);

            if (!bloomFilter.mightExist(code)) {
                bloomFilter.add(code);
                return code;
            }
        }

        return fallback(hash);
    }

    private String fallback(String hash) {
        String base = hash.substring(0, 6);
        String suffix = Integer.toHexString((int)(secureRandom.nextInt(ALPHABET.length) * 256));
        suffix = suffix.length() == 1 ? "0" + suffix : suffix;
        String code = base + suffix;

        bloomFilter.add(code);
        return code;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }
}
package com.sportshop.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordService {
    private static final int SALT_BYTES = 16;

    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    public UserPasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generateSalt() {
        byte[] bytes = new byte[SALT_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String encode(String rawPassword, String salt) {
        return passwordEncoder.encode(rawPassword + salt);
    }

    public boolean matches(String rawPassword, String salt, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (salt == null || salt.isBlank()) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
        return passwordEncoder.matches(rawPassword + salt, encodedPassword);
    }
}

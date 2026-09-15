package com.nutriai.modules.auth.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes

    private final ConcurrentMap<String, AttemptData> attemptsCache = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        AttemptData data = attemptsCache.get(key.toLowerCase());
        if (data == null) {
            return false;
        }

        if (data.isLockoutExpired()) {
            attemptsCache.remove(key.toLowerCase());
            return false;
        }

        return data.attempts >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        cleanOldEntries();
        attemptsCache.compute(key.toLowerCase(), (k, v) -> {
            if (v == null || v.isLockoutExpired()) {
                return new AttemptData(1, Instant.now().plusMillis(LOCKOUT_DURATION_MS));
            }
            return new AttemptData(v.attempts + 1, Instant.now().plusMillis(LOCKOUT_DURATION_MS));
        });
    }

    public void recordSuccess(String key) {
        attemptsCache.remove(key.toLowerCase());
    }

    public void reset() {
        attemptsCache.clear();
    }

    private void cleanOldEntries() {
        if (attemptsCache.size() > 1000) {
            attemptsCache.entrySet().removeIf(entry -> entry.getValue().isLockoutExpired());
        }
    }

    private record AttemptData(int attempts, Instant lockoutExpiresAt) {
        boolean isLockoutExpired() {
            return Instant.now().isAfter(lockoutExpiresAt);
        }
    }
}

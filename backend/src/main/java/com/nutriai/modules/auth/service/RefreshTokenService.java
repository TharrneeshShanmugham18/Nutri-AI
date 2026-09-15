package com.nutriai.modules.auth.service;

import com.nutriai.common.exception.ApiException;
import com.nutriai.modules.auth.domain.AuditEventType;
import com.nutriai.modules.auth.domain.RefreshToken;
import com.nutriai.modules.auth.domain.User;
import com.nutriai.modules.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditService auditService;
    private final long refreshTokenExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            AuditService auditService,
            @Value("${app.security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditService = auditService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs);

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public RotationResult rotateRefreshToken(String rawToken, String ipAddress, String userAgent) {
        String tokenHash = hashToken(rawToken);
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        RefreshToken currentToken = tokenOpt.get();
        User user = currentToken.getUser();

        // Check for token reuse (stolen token replay attempt)
        if (currentToken.isRevoked() || currentToken.getReplacedByTokenHash() != null) {
            // Revoke all tokens for this user immediately
            refreshTokenRepository.revokeAllByUserId(user.getId());
            auditService.recordEvent(
                    AuditEventType.TOKEN_REUSE_DETECTED,
                    user.getId(),
                    user.getEmail(),
                    ipAddress,
                    userAgent,
                    "Revoked all sessions due to refresh token reuse attempt"
            );
            throw new BadCredentialsException("Refresh token reuse detected. All sessions invalidated.");
        }

        if (currentToken.isExpired()) {
            currentToken.setRevoked(true);
            refreshTokenRepository.save(currentToken);
            throw new BadCredentialsException("Refresh token has expired");
        }

        // Generate new rotated token
        String newRawToken = generateSecureToken();
        String newTokenHash = hashToken(newRawToken);
        Instant newExpiresAt = Instant.now().plusMillis(refreshTokenExpirationMs);

        // Mark old token as revoked and replaced
        currentToken.setRevoked(true);
        currentToken.setReplacedByTokenHash(newTokenHash);
        refreshTokenRepository.save(currentToken);

        // Save new token
        RefreshToken newRefreshToken = new RefreshToken(user, newTokenHash, newExpiresAt);
        refreshTokenRepository.save(newRefreshToken);

        return new RotationResult(newRawToken, user);
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new ApiException("SHA-256 algorithm not available", e);
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public record RotationResult(String newRawToken, User user) {}
}

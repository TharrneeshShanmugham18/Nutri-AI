package com.nutriai.modules.auth.service;

import com.nutriai.common.exception.AccountLockedException;
import com.nutriai.common.exception.ResourceConflictException;
import com.nutriai.modules.auth.domain.*;
import com.nutriai.modules.auth.dto.AuthResponse;
import com.nutriai.modules.auth.dto.LoginRequest;
import com.nutriai.modules.auth.dto.RegisterRequest;
import com.nutriai.modules.auth.dto.UserDto;
import com.nutriai.modules.auth.repository.RoleRepository;
import com.nutriai.modules.auth.repository.UserRepository;
import com.nutriai.modules.auth.security.JwtService;
import com.nutriai.modules.auth.security.LoginAttemptService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            LoginAttemptService loginAttemptService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
    }

    @Transactional
    public UserDto register(RegisterRequest request, String ipAddress, String userAgent) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResourceConflictException("An account with this email already exists");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER, "Standard user role")));

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(normalizedEmail, encodedPassword);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        auditService.recordEvent(
                AuditEventType.ACCOUNT_CREATED,
                savedUser.getId(),
                savedUser.getEmail(),
                ipAddress,
                userAgent,
                "User account registered successfully"
        );

        return UserDto.fromEntity(savedUser);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public LoginResult login(LoginRequest request, String ipAddress, String userAgent) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String rateLimitKey = ipAddress + ":" + normalizedEmail;

        if (loginAttemptService.isBlocked(rateLimitKey)) {
            auditService.recordEvent(
                    AuditEventType.ACCOUNT_LOCKED,
                    null,
                    normalizedEmail,
                    ipAddress,
                    userAgent,
                    "Login rejected: rate-limit / lockout active"
            );
            throw new AccountLockedException("Too many failed attempts. Please try again later.");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.recordFailure(rateLimitKey);
            auditService.recordEvent(
                    AuditEventType.LOGIN_FAILED,
                    user != null ? user.getId() : null,
                    normalizedEmail,
                    ipAddress,
                    userAgent,
                    "Invalid email or password"
            );
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new LockedException("User account is locked");
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new DisabledException("User account is disabled");
        }

        loginAttemptService.recordSuccess(rateLimitKey);

        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleNames);
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);

        auditService.recordEvent(
                AuditEventType.LOGIN_SUCCESS,
                user.getId(),
                user.getEmail(),
                ipAddress,
                userAgent,
                "User logged in successfully"
        );

        UserDto userDto = UserDto.fromEntity(user);
        AuthResponse authResponse = AuthResponse.of(accessToken, jwtService.getAccessTokenExpirationMs(), userDto);

        return new LoginResult(authResponse, rawRefreshToken);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public LoginResult refresh(String rawRefreshToken, String ipAddress, String userAgent) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotateRefreshToken(rawRefreshToken, ipAddress, userAgent);
        User user = result.user();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("User account is not active");
        }

        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleNames);

        auditService.recordEvent(
                AuditEventType.TOKEN_REFRESHED,
                user.getId(),
                user.getEmail(),
                ipAddress,
                userAgent,
                "Access token refreshed and refresh token rotated"
        );

        UserDto userDto = UserDto.fromEntity(user);
        AuthResponse authResponse = AuthResponse.of(accessToken, jwtService.getAccessTokenExpirationMs(), userDto);

        return new LoginResult(authResponse, result.newRawToken());
    }

    @Transactional
    public void logout(String rawRefreshToken, UUID userId, String email, String ipAddress, String userAgent) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(rawRefreshToken);
        }
        auditService.recordEvent(
                AuditEventType.LOGOUT,
                userId,
                email,
                ipAddress,
                userAgent,
                "User logged out successfully"
        );
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return UserDto.fromEntity(user);
    }

    public record LoginResult(AuthResponse authResponse, String rawRefreshToken) {}
}

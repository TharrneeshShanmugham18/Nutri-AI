package com.nutriai.modules.auth;

import com.nutriai.modules.auth.domain.*;
import com.nutriai.modules.auth.dto.LoginRequest;
import com.nutriai.modules.auth.dto.RegisterRequest;
import com.nutriai.modules.auth.repository.AuditLogRepository;
import com.nutriai.modules.auth.repository.RefreshTokenRepository;
import com.nutriai.modules.auth.repository.RoleRepository;
import com.nutriai.modules.auth.repository.UserRepository;
import com.nutriai.modules.auth.security.JwtService;
import com.nutriai.modules.auth.security.LoginAttemptService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        loginAttemptService.reset();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should successfully register user with valid email and password")
        void testValidRegistration() throws Exception {
            RegisterRequest request = new RegisterRequest("testuser@example.com", "P@ssword123!");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.email").value("testuser@example.com"))
                    .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());

            User user = userRepository.findByEmailIgnoreCase("testuser@example.com").orElseThrow();
            assertThat(user.getPasswordHash()).isNotEqualTo("P@ssword123!");
            assertThat(passwordEncoder.matches("P@ssword123!", user.getPasswordHash())).isTrue();
        }

        @Test
        @DisplayName("Should reject registration with duplicate email (409 Conflict)")
        void testDuplicateEmail() throws Exception {
            RegisterRequest request = new RegisterRequest("duplicate@example.com", "P@ssword123!");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("An account with this email already exists"));
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void testInvalidEmail() throws Exception {
            RegisterRequest request = new RegisterRequest("invalid-email-format", "P@ssword123!");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").isArray());
        }

        @Test
        @DisplayName("Should reject weak password (too short or missing required character sets)")
        void testWeakPassword() throws Exception {
            RegisterRequest shortPass = new RegisterRequest("short@example.com", "P@1a");
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shortPass)))
                    .andExpect(status().isBadRequest());

            RegisterRequest noSpecialChar = new RegisterRequest("nospecial@example.com", "Password1234");
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(noSpecialChar)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login & JWT Tests")
    class LoginTests {

        @BeforeEach
        void registerUser() throws Exception {
            RegisterRequest request = new RegisterRequest("loginuser@example.com", "P@ssword123!");
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should successfully login and return access token and HttpOnly refresh cookie")
        void testValidLogin() throws Exception {
            LoginRequest request = new LoginRequest("loginuser@example.com", "P@ssword123!");

            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(900000))
                    .andExpect(jsonPath("$.user.email").value("loginuser@example.com"))
                    .andExpect(cookie().exists("refreshToken"))
                    .andExpect(cookie().httpOnly("refreshToken", true))
                    .andExpect(cookie().path("refreshToken", "/api/v1/auth"))
                    .andReturn();

            String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
            assertThat(setCookie).contains("SameSite=Lax");
        }

        @Test
        @DisplayName("Should reject login with incorrect password (401 Unauthorized)")
        void testIncorrectPassword() throws Exception {
            LoginRequest request = new LoginRequest("loginuser@example.com", "WrongPassword123!");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("Should reject non-existent user with identical generic error to prevent enumeration")
        void testNonExistentUser() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent@example.com", "P@ssword123!");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("Should lock out account after 5 consecutive failed attempts")
        void testBruteForceLockout() throws Exception {
            LoginRequest wrongRequest = new LoginRequest("loginuser@example.com", "WrongPass123!");

            // Trigger 5 failed attempts
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(wrongRequest)))
                        .andExpect(status().isUnauthorized());
            }

            // 6th attempt should be rejected with rate limit / locked exception
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Account is temporarily locked due to excessive failed attempts. Please try again later."));
        }
    }

    @Nested
    @DisplayName("Protected Endpoints & Authorization Tests")
    class ProtectedEndpointTests {

        private String validAccessToken;
        private UUID userId;

        @BeforeEach
        void createTestUser() {
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
            User user = new User("protected@example.com", passwordEncoder.encode("P@ssword123!"));
            user.addRole(userRole);
            User saved = userRepository.save(user);
            this.userId = saved.getId();
            this.validAccessToken = jwtService.generateAccessToken(saved.getId(), saved.getEmail(), List.of("ROLE_USER"));
        }

        @Test
        @DisplayName("Should access /api/v1/auth/me with valid Bearer token")
        void testAccessMeWithValidToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.email").value("protected@example.com"));
        }

        @Test
        @DisplayName("Should reject access without token (401)")
        void testAccessMeWithoutToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("Should reject access with malformed/tampered token (401)")
        void testAccessMeWithTamperedToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken + "tampered"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should deny standard USER from accessing ADMIN-only endpoint (403 Forbidden)")
        void testRoleBasedAccessDeniedForStandardUser() throws Exception {
            mockMvc.perform(get("/api/v1/auth/test-admin")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("Forbidden"));
        }

        @Test
        @DisplayName("Should allow ADMIN role to access ADMIN-only endpoint (200 OK)")
        void testRoleBasedAccessAllowedForAdmin() throws Exception {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN, "Admin role")));
            User admin = new User("admin@nutriai.com", passwordEncoder.encode("AdminPass123!"));
            admin.addRole(adminRole);
            User savedAdmin = userRepository.save(admin);

            String adminToken = jwtService.generateAccessToken(savedAdmin.getId(), savedAdmin.getEmail(), List.of("ROLE_ADMIN"));

            mockMvc.perform(get("/api/v1/auth/test-admin")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Admin access granted"));
        }
    }

    @Nested
    @DisplayName("Refresh Token & Logout Tests")
    class RefreshAndLogoutTests {

        private Cookie refreshCookie;

        @BeforeEach
        void loginAndGetCookie() throws Exception {
            RegisterRequest reg = new RegisterRequest("refresh@example.com", "P@ssword123!");
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());

            LoginRequest login = new LoginRequest("refresh@example.com", "P@ssword123!");
            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andReturn();

            this.refreshCookie = result.getResponse().getCookie("refreshToken");
            assertThat(refreshCookie).isNotNull();
        }

        @Test
        @DisplayName("Should rotate refresh token and return new access token")
        void testTokenRefresh() throws Exception {
            MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(cookie().exists("refreshToken"))
                    .andReturn();

            Cookie newCookie = refreshResult.getResponse().getCookie("refreshToken");
            assertThat(newCookie).isNotNull();
            assertThat(newCookie.getValue()).isNotEqualTo(refreshCookie.getValue());
        }

        @Test
        @DisplayName("Should detect refresh token reuse and invalidate all sessions")
        void testRefreshTokenReuseDetection() throws Exception {
            // First refresh: rotates token successfully
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie))
                    .andExpect(status().isOk());

            // Second refresh attempt with OLD token (replay attack simulation): should trigger reuse detection
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie))
                    .andExpect(status().isUnauthorized());

            // All tokens for user should now be revoked
            User user = userRepository.findByEmailIgnoreCase("refresh@example.com").orElseThrow();
            List<RefreshToken> allTokens = refreshTokenRepository.findAll();
            assertThat(allTokens).allMatch(RefreshToken::isRevoked);
        }

        @Test
        @DisplayName("Should revoke token on logout and clear cookie")
        void testLogout() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(refreshCookie))
                    .andExpect(status().isOk())
                    .andExpect(cookie().maxAge("refreshToken", 0));

            // Subsequent refresh with that token should fail
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .cookie(refreshCookie))
                    .andExpect(status().isUnauthorized());
        }
    }
}

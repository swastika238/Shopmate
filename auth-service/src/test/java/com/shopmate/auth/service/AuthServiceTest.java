package com.shopmate.auth.service;

import com.shopmate.auth.dto.AuthResponse;
import com.shopmate.auth.dto.LoginRequest;
import com.shopmate.auth.dto.RegisterRequest;
import com.shopmate.auth.exception.ApiException;
import com.shopmate.auth.model.Role;
import com.shopmate.auth.model.User;
import com.shopmate.auth.repository.UserRepository;
import com.shopmate.auth.util.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = new JwtService(
                "ShopMateSuperSecretKeyForJWTAuthentication123456789",
                900000L,
                604800000L
        );
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void register_newEmail_returnsTokensAndUser() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        AuthResponse res = authService.register(req);

        assertNotNull(res.getAccessToken());
        assertNotNull(res.getRefreshToken());
        assertEquals("alice@example.com", res.getUser().getEmail());
        assertEquals(Role.CUSTOMER, res.getUser().getRole());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.register(req));
    }

    @Test
    void login_validCredentials_returnsTokens() {
        User user = new User("Alice", "alice@example.com", passwordEncoder.encode("Password123"), Role.CUSTOMER);
        user.setId(2L);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("Password123");

        AuthResponse res = authService.login(req);

        assertNotNull(res.getAccessToken());
        assertEquals(2L, res.getUser().getId());
    }
}

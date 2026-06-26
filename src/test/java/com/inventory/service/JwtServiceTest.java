package com.inventory.service;

import com.inventory.entity.AppUser;
import com.inventory.entity.Role;
import com.inventory.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "7638792F423F4428472B4B6250645367566B597033733676397924422645294840");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);

        testUser = AppUser.builder()
                .id(1L)
                .username("testadmin")
                .password("encoded")
                .role(Role.ADMINISTRATOR)
                .build();
    }

    @Test
    @DisplayName("generateToken - should produce non-blank token")
    void generateToken_shouldReturnNonBlankToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername - should return correct username from token")
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testadmin");
    }

    @Test
    @DisplayName("isTokenValid - valid token for same user should return true")
    void isTokenValid_validToken_shouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - token for different user should return false")
    void isTokenValid_differentUser_shouldReturnFalse() {
        String token = jwtService.generateToken(testUser);

        AppUser otherUser = AppUser.builder()
                .id(2L)
                .username("otheruser")
                .password("encoded")
                .role(Role.VIEWER)
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid - expired token should return false")
    void isTokenValid_expiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L); // already expired
        String token = jwtService.generateToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isFalse();
    }
}

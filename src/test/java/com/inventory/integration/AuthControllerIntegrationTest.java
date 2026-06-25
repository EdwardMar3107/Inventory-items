package com.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.AuthRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auth API Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/login - admin credentials should return JWT token")
    void login_withAdminCredentials_shouldReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("token");
        assertThat(response.getBody()).contains("ADMINISTRATOR");
    }

    @Test
    @DisplayName("POST /api/auth/login - viewer credentials should return JWT token")
    void login_withViewerCredentials_shouldReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("viewer");
        request.setPassword("viewer123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("token");
        assertThat(response.getBody()).contains("VIEWER");
    }

    @Test
    @DisplayName("POST /api/auth/login - invalid password should return 401")
    void login_withInvalidPassword_shouldReturn401() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POST /api/auth/login - blank username should return 400")
    void login_withBlankUsername_shouldReturn400() {
        AuthRequest request = new AuthRequest();
        request.setUsername("");
        request.setPassword("admin123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

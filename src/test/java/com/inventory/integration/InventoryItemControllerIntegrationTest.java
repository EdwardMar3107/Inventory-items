package com.inventory.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.AuthRequest;
import com.inventory.dto.AuthResponse;
import com.inventory.dto.InventoryItemRequest;
import com.inventory.entity.Category;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inventory Items API Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryItemControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String viewerToken;

    @BeforeEach
    void setUp() {
        adminToken = getToken("admin", "admin123");
        viewerToken = getToken("viewer", "viewer123");
    }

    private String getToken(String username, String password) {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getToken();
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private InventoryItemRequest buildRequest(String sku) {
        InventoryItemRequest req = new InventoryItemRequest();
        req.setName("Wireless Mouse");
        req.setSku(sku);
        req.setQuantity(100);
        req.setPrice(new BigDecimal("29.99"));
        req.setCategory(Category.ELECTRONICS);
        req.setLocation("warehouse-1");
        req.setDescription("Ergonomic wireless mouse");
        return req;
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/items - admin should create item and return 201")
    void createItem_asAdmin_shouldReturn201() {
        InventoryItemRequest req = buildRequest("WM-CREATE-TEST");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/items", HttpMethod.POST,
                new HttpEntity<>(req, bearerHeaders(adminToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("WM-CREATE-TEST");
        assertThat(response.getBody()).contains("ELECTRONICS");
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/items - viewer should get 403")
    void createItem_asViewer_shouldReturn403() {
        InventoryItemRequest req = buildRequest("WM-VIEWER-FORBIDDEN");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/items", HttpMethod.POST,
                new HttpEntity<>(req, bearerHeaders(viewerToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/items - viewer can list items")
    void listItems_asViewer_shouldReturn200() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/items", HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(viewerToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
        assertThat(response.getBody()).contains("totalElements");
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/items - unauthenticated should return 401")
    void listItems_unauthenticated_shouldReturn401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/items", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/items - duplicate SKU should return 409")
    void createItem_duplicateSku_shouldReturn409() {
        InventoryItemRequest req = buildRequest("WM-DUPLICATE");

        restTemplate.exchange("/api/items", HttpMethod.POST,
                new HttpEntity<>(req, bearerHeaders(adminToken)), String.class);

        ResponseEntity<String> second = restTemplate.exchange(
                "/api/items", HttpMethod.POST,
                new HttpEntity<>(req, bearerHeaders(adminToken)), String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/items - invalid name should return 400")
    void createItem_invalidName_shouldReturn400() {
        InventoryItemRequest req = buildRequest("WM-BADNAME");
        req.setName("ab"); // too short — min 3 chars

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/items", HttpMethod.POST,
                new HttpEntity<>(req, bearerHeaders(adminToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("fieldErrors");
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/items/{id} - non-existing id should return 404")
    void getById_nonExisting_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/items/999999", HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(adminToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(8)
    @DisplayName("DELETE /api/items/{id} - admin should delete item and return 204")
    void deleteItem_asAdmin_shouldReturn204() throws Exception {
        // Create item
        InventoryItemRequest req = buildRequest("WM-TO-DELETE");
        ResponseEntity<String> created = restTemplate.exchange(
                "/api/items", HttpMethod.POST,
                new HttpEntity<>(req, bearerHeaders(adminToken)), String.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Parse ID properly via Jackson
        JsonNode json = objectMapper.readTree(created.getBody());
        long id = json.get("id").asLong();

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/items/" + id, HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(adminToken)), Void.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(9)
    @DisplayName("PUT /api/items/{id} - admin should update item and return 200")
    void updateItem_asAdmin_shouldReturn200() throws Exception {
        // Create item
        InventoryItemRequest req = buildRequest("WM-TO-UPDATE");
        ResponseEntity<String> created = restTemplate.exchange(
                "/api/items", HttpMethod.POST,
                new HttpEntity<>(req, bearerHeaders(adminToken)), String.class);

        long id = objectMapper.readTree(created.getBody()).get("id").asLong();

        // Update
        req.setName("Updated Mouse");
        req.setQuantity(200);

        ResponseEntity<String> updated = restTemplate.exchange(
                "/api/items/" + id, HttpMethod.PUT,
                new HttpEntity<>(req, bearerHeaders(adminToken)), String.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).contains("Updated Mouse");
        assertThat(updated.getBody()).contains("200");
    }
}

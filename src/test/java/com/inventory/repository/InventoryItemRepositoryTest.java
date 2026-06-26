package com.inventory.repository;

import com.inventory.entity.Category;
import com.inventory.entity.InventoryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration"
})
@DisplayName("InventoryItemRepository Tests")
class InventoryItemRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("inventory_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private InventoryItemRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.save(InventoryItem.builder()
                .name("Wireless Mouse").sku("WM-001").quantity(100)
                .price(new BigDecimal("29.99")).category(Category.ELECTRONICS).build());
        repository.save(InventoryItem.builder()
                .name("Office Chair").sku("OC-001").quantity(20)
                .price(new BigDecimal("199.99")).category(Category.FURNITURE).build());
        repository.save(InventoryItem.builder()
                .name("Wireless Keyboard").sku("WK-001").quantity(50)
                .price(new BigDecimal("49.99")).category(Category.ELECTRONICS).build());
    }

    @Test
    @DisplayName("findAllWithFilters - no filters should return all items")
    void findAllWithFilters_noFilters_shouldReturnAll() {
        Page<InventoryItem> result = repository.findAllWithFilters(
                null, null, null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("findAllWithFilters - filter by name partial match")
    void findAllWithFilters_byName_shouldReturnMatching() {
        Page<InventoryItem> result = repository.findAllWithFilters(
                "wireless", null, null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllWithFilters - filter by category")
    void findAllWithFilters_byCategory_shouldReturnMatching() {
        Page<InventoryItem> result = repository.findAllWithFilters(
                null, null, Category.ELECTRONICS, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllWithFilters - filter by SKU partial match")
    void findAllWithFilters_bySku_shouldReturnMatching() {
        Page<InventoryItem> result = repository.findAllWithFilters(
                null, "WM", null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("existsBySku - existing sku should return true")
    void existsBySku_existingSku_shouldReturnTrue() {
        assertThat(repository.existsBySku("WM-001")).isTrue();
    }

    @Test
    @DisplayName("existsBySku - non-existing sku should return false")
    void existsBySku_nonExistingSku_shouldReturnFalse() {
        assertThat(repository.existsBySku("NONEXISTENT")).isFalse();
    }

    @Test
    @DisplayName("existsBySkuAndIdNot - same sku for different id should return true")
    void existsBySkuAndIdNot_shouldReturnTrue() {
        InventoryItem item = repository.findBySku("WM-001").orElseThrow();
        assertThat(repository.existsBySkuAndIdNot("WM-001", item.getId() + 1)).isTrue();
    }
}

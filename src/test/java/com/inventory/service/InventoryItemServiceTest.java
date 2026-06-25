package com.inventory.service;

import com.inventory.dto.*;
import com.inventory.entity.Category;
import com.inventory.entity.InventoryItem;
import com.inventory.exception.DuplicateSkuException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryItemRepository;
import com.inventory.service.impl.InventoryItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryItemService Unit Tests")
class InventoryItemServiceTest {

    @Mock
    private InventoryItemRepository repository;

    @Mock
    private InventoryItemMapper mapper;

    @InjectMocks
    private InventoryItemServiceImpl service;

    private InventoryItem sampleItem;
    private InventoryItemRequest sampleRequest;
    private InventoryItemResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleItem = InventoryItem.builder()
                .id(1L)
                .name("Wireless Mouse")
                .sku("WM-12345")
                .quantity(100)
                .price(new BigDecimal("29.99"))
                .category(Category.ELECTRONICS)
                .location("warehouse-1")
                .description("Ergonomic wireless mouse")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new InventoryItemRequest();
        sampleRequest.setName("Wireless Mouse");
        sampleRequest.setSku("WM-12345");
        sampleRequest.setQuantity(100);
        sampleRequest.setPrice(new BigDecimal("29.99"));
        sampleRequest.setCategory(Category.ELECTRONICS);
        sampleRequest.setLocation("warehouse-1");
        sampleRequest.setDescription("Ergonomic wireless mouse");

        sampleResponse = new InventoryItemResponse();
        sampleResponse.setId(1L);
        sampleResponse.setName("Wireless Mouse");
        sampleResponse.setSku("WM-12345");
        sampleResponse.setQuantity(100);
        sampleResponse.setPrice(new BigDecimal("29.99"));
        sampleResponse.setCategory(Category.ELECTRONICS);
    }

    @Test
    @DisplayName("findById - existing id should return item")
    void findById_existingId_shouldReturnItem() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(mapper.toResponse(sampleItem)).thenReturn(sampleResponse);

        InventoryItemResponse result = service.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("WM-12345");
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("findById - non-existing id should throw ResourceNotFoundException")
    void findById_nonExistingId_shouldThrowNotFoundException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("create - valid request should create and return item")
    void create_validRequest_shouldReturnCreatedItem() {
        when(repository.existsBySku("WM-12345")).thenReturn(false);
        when(mapper.toEntity(sampleRequest)).thenReturn(sampleItem);
        when(repository.save(sampleItem)).thenReturn(sampleItem);
        when(mapper.toResponse(sampleItem)).thenReturn(sampleResponse);

        InventoryItemResponse result = service.create(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("WM-12345");
        verify(repository).save(sampleItem);
    }

    @Test
    @DisplayName("create - duplicate SKU should throw DuplicateSkuException")
    void create_duplicateSku_shouldThrowDuplicateSkuException() {
        when(repository.existsBySku("WM-12345")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("WM-12345");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - valid request should update and return item")
    void update_validRequest_shouldReturnUpdatedItem() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(repository.existsBySkuAndIdNot("WM-12345", 1L)).thenReturn(false);
        when(repository.save(sampleItem)).thenReturn(sampleItem);
        when(mapper.toResponse(sampleItem)).thenReturn(sampleResponse);

        InventoryItemResponse result = service.update(1L, sampleRequest);

        assertThat(result).isNotNull();
        verify(mapper).updateEntity(sampleRequest, sampleItem);
        verify(repository).save(sampleItem);
    }

    @Test
    @DisplayName("update - non-existing id should throw ResourceNotFoundException")
    void update_nonExistingId_shouldThrowNotFoundException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, sampleRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete - existing id should delete item")
    void delete_existingId_shouldDeleteItem() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - non-existing id should throw ResourceNotFoundException")
    void delete_nonExistingId_shouldThrowNotFoundException() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("findAll - should return paginated results")
    void findAll_shouldReturnPagedResponse() {
        // Use any(Pageable.class) — the service builds Pageable internally with Sort,
        // so we must not match on exact PageRequest instance.
        Page<InventoryItem> page = new PageImpl<>(List.of(sampleItem),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")), 1);

        when(repository.findAllWithFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(mapper.toResponse(sampleItem)).thenReturn(sampleResponse);

        PagedResponse<InventoryItemResponse> result = service.findAll(null, null, null, 0, 20, null);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();
    }
}

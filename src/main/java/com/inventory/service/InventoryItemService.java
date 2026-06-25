package com.inventory.service;

import com.inventory.dto.InventoryItemRequest;
import com.inventory.dto.InventoryItemResponse;
import com.inventory.dto.PagedResponse;
import com.inventory.dto.StatsResponse;
import com.inventory.entity.Category;

import java.time.LocalDate;

public interface InventoryItemService {

    PagedResponse<InventoryItemResponse> findAll(
            String name,
            String sku,
            Category category,
            int page,
            int size,
            String sort
    );

    InventoryItemResponse findById(Long id);

    InventoryItemResponse create(InventoryItemRequest request);

    InventoryItemResponse update(Long id, InventoryItemRequest request);

    void delete(Long id);

    StatsResponse getStats(LocalDate from, LocalDate to);
}

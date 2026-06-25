package com.inventory.service.impl;

import com.inventory.dto.*;
import com.inventory.entity.Category;
import com.inventory.entity.InventoryItem;
import com.inventory.exception.DuplicateSkuException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryItemRepository;
import com.inventory.repository.InventoryItemRepository.CategoryStatProjection;
import com.inventory.service.InventoryItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository repository;
    private final InventoryItemMapper mapper;

    @Override
    public PagedResponse<InventoryItemResponse> findAll(
            String name, String sku, Category category,
            int page, int size, String sort) {

        Sort sortOrder = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<InventoryItem> itemPage = repository.findAllWithFilters(
                isEmpty(name) ? null : name,
                isEmpty(sku) ? null : sku,
                category,
                pageable
        );

        List<InventoryItemResponse> content = itemPage.getContent()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                itemPage.getNumber(),
                itemPage.getSize(),
                itemPage.getTotalElements(),
                itemPage.getTotalPages(),
                itemPage.isLast()
        );
    }

    @Override
    public InventoryItemResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public InventoryItemResponse create(InventoryItemRequest request) {
        if (repository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        InventoryItem item = mapper.toEntity(request);
        InventoryItem saved = repository.save(item);
        log.info("Created inventory item id={} sku={}", saved.getId(), saved.getSku());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InventoryItemResponse update(Long id, InventoryItemRequest request) {
        InventoryItem item = getOrThrow(id);

        if (repository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new DuplicateSkuException(request.getSku());
        }

        mapper.updateEntity(request, item);
        InventoryItem saved = repository.save(item);
        log.info("Updated inventory item id={}", id);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        repository.deleteById(id);
        log.info("Deleted inventory item id={}", id);
    }

    @Override
    public StatsResponse getStats(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must not be after 'to' date");
        }

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        Long totalCount = repository.countByDateRange(fromDt, toDt);
        List<CategoryStatProjection> stats = repository.getStatsByDateRange(fromDt, toDt);

        Map<Category, Long> countPerCategory = stats.stream()
                .collect(Collectors.toMap(CategoryStatProjection::getCategory, CategoryStatProjection::getItemCount));

        Map<Category, Long> quantityPerCategory = stats.stream()
                .collect(Collectors.toMap(CategoryStatProjection::getCategory, CategoryStatProjection::getTotalQuantity));

        return new StatsResponse(totalCount, countPerCategory, quantityPerCategory,
                from.toString(), to.toString());
    }

    private InventoryItem getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Set<String> allowedFields = Set.of("id", "name", "sku", "price", "quantity", "category", "createdAt", "updatedAt");
        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }
        return Sort.by(direction, field);
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }
}

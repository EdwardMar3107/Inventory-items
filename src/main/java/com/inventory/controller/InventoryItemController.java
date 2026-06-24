package com.inventory.controller;

import com.inventory.dto.*;
import com.inventory.entity.Category;
import com.inventory.service.InventoryItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Inventory Items", description = "CRUD operations for inventory items")
@SecurityRequirement(name = "bearerAuth")
public class InventoryItemController {

    private final InventoryItemService service;

    @GetMapping("/items")
    @Operation(summary = "List all items", description = "Get paginated, filtered, and sorted list of inventory items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedResponse<InventoryItemResponse>> getAll(
            @Parameter(description = "Filter by name (partial match)") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by SKU (partial match)") @RequestParam(required = false) String sku,
            @Parameter(description = "Filter by category") @RequestParam(required = false) Category category,
            @Parameter(description = "Page number (0-based)") @RequestParam(name = "_page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(name = "_size", defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction, e.g. 'name,asc' or 'price,desc'")
            @RequestParam(name = "_sort", required = false) String sort
    ) {
        return ResponseEntity.ok(service.findAll(name, sku, category, page, size, sort));
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get item by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item found"),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/items")
    @Operation(summary = "Create new item", description = "Requires ADMINISTRATOR role")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "SKU already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryItemResponse> create(@Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update existing item", description = "Requires ADMINISTRATOR role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "SKU already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryItemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Delete item", description = "Requires ADMINISTRATOR role")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item deleted"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get inventory statistics by date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned"),
            @ApiResponse(responseCode = "400", description = "Invalid date range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StatsResponse> getStats(
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(service.getStats(from, to));
    }
}

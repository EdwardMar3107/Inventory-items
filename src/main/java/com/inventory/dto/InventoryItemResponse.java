package com.inventory.dto;

import com.inventory.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Inventory item response")
public class InventoryItemResponse {

    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "Item name", example = "Wireless Mouse")
    private String name;

    @Schema(description = "SKU", example = "WM-12345")
    private String sku;

    @Schema(description = "Available quantity", example = "100")
    private Integer quantity;

    @Schema(description = "Unit price", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Category", example = "ELECTRONICS")
    private Category category;

    @Schema(description = "Storage location", example = "warehouse-1")
    private String location;

    @Schema(description = "Description", example = "Ergonomic wireless mouse")
    private String description;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

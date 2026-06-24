package com.inventory.dto;

import com.inventory.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Inventory item create/update request")
public class InventoryItemRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Schema(description = "Item name", example = "Wireless Mouse", minLength = 3, maxLength = 50)
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 30, message = "SKU must not exceed 30 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9\\-_]*$",
            message = "SKU must contain only Latin letters, digits, hyphens and underscores"
    )
    @Schema(description = "Stock Keeping Unit (unique)", example = "WM-12345", maxLength = 30)
    private String sku;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be >= 0")
    @Schema(description = "Available quantity", example = "100", minimum = "0")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be >= 0.00")
    @Digits(integer = 17, fraction = 2, message = "Price must have at most 2 decimal places")
    @Schema(description = "Unit price", example = "29.99", minimum = "0.00")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    @Schema(description = "Item category", example = "ELECTRONICS",
            allowableValues = {"ELECTRONICS", "FURNITURE", "GROCERY", "CLOTHING"})
    private Category category;

    @Size(max = 40, message = "Location must not exceed 40 characters")
    @Schema(description = "Storage location", example = "warehouse-1", maxLength = 40)
    private String location;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Item description", example = "Ergonomic wireless mouse", maxLength = 500)
    private String description;
}

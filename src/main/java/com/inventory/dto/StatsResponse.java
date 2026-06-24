package com.inventory.dto;

import com.inventory.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Schema(description = "Inventory statistics response")
public class StatsResponse {

    @Schema(description = "Total item count in date range")
    private Long totalCount;

    @Schema(description = "Item count per category")
    private Map<Category, Long> countPerCategory;

    @Schema(description = "Total quantity per category")
    private Map<Category, Long> quantityPerCategory;

    @Schema(description = "Date range from")
    private String from;

    @Schema(description = "Date range to")
    private String to;
}

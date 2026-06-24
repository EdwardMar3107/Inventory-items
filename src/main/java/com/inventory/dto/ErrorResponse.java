package com.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "Error timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "HTTP error description", example = "Not Found")
    private String error;

    @Schema(description = "Error message", example = "Item not found with id: 42")
    private String message;

    @Schema(description = "Request path", example = "/api/items/42")
    private String path;

    @Schema(description = "Field validation errors")
    private Map<String, String> fieldErrors;
}

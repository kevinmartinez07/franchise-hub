package com.kevinmartinez.franchise.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddProductRequest(
        @NotBlank(message = "El branchId es obligatorio")
        String branchId,
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock) {
}

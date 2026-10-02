package com.kevinmartinez.franchise.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateNameRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name) {
}

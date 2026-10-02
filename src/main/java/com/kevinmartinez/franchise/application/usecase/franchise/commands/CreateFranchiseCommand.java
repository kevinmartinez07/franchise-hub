package com.kevinmartinez.franchise.application.usecase.franchise.commands;

import jakarta.validation.constraints.NotBlank;

public record CreateFranchiseCommand(
        @NotBlank(message = "El nombre es obligatorio")
        String name) {
}

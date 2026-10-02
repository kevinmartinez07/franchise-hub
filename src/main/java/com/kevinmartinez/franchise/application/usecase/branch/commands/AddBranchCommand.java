package com.kevinmartinez.franchise.application.usecase.branch.commands;

import jakarta.validation.constraints.NotBlank;

public record AddBranchCommand(
        @NotBlank(message = "El franchiseId es obligatorio") String franchiseId,
        @NotBlank(message = "El nombre es obligatorio") String name) {
}

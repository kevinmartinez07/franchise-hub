package com.kevinmartinez.franchise.application.usecase.branch.dto;

import java.time.Instant;

import com.kevinmartinez.franchise.domain.model.Branch;

public record BranchDto(
        String id,
        String franchiseId,
        String name,
        Instant createdAt,
        Instant updatedAt) {

    public static BranchDto from(Branch branch) {
        return new BranchDto(
                branch.getId(),
                branch.getFranchiseId(),
                branch.getName(),
                branch.getCreatedAt(),
                branch.getUpdatedAt());
    }
}

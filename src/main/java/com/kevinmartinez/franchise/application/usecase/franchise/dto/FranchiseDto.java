package com.kevinmartinez.franchise.application.usecase.franchise.dto;

import java.time.Instant;

import com.kevinmartinez.franchise.domain.model.Franchise;

public record FranchiseDto(
        String id,
        String name,
        Instant createdAt,
        Instant updatedAt) {

    public static FranchiseDto from(Franchise franchise) {
        return new FranchiseDto(
                franchise.getId(),
                franchise.getName(),
                franchise.getCreatedAt(),
                franchise.getUpdatedAt());
    }
}

package com.kevinmartinez.franchise.application.usecase.product.dto;

import java.time.Instant;

import com.kevinmartinez.franchise.domain.model.Product;

public record ProductDto(
        String id,
        String branchId,
        String name,
        int stock,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getBranchId(),
                product.getName(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}

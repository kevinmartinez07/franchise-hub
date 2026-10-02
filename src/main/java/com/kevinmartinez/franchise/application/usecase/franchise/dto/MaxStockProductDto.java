package com.kevinmartinez.franchise.application.usecase.franchise.dto;

public record MaxStockProductDto(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock) {
}

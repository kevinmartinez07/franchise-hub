package com.kevinmartinez.franchise.application.usecase.product.commands;

public record UpdateProductStockCommand(String productId, int stock) {
}

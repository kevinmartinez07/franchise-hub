package com.kevinmartinez.franchise.application.usecase.product.commands;

public record AddProductCommand(String branchId, String name, int stock) {
}

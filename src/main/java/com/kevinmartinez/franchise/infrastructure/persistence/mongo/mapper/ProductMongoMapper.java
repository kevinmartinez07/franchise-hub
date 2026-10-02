package com.kevinmartinez.franchise.infrastructure.persistence.mongo.mapper;

import com.kevinmartinez.franchise.domain.model.Product;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.document.ProductDocument;

public final class ProductMongoMapper {

    private ProductMongoMapper() {
    }

    public static ProductDocument toDocument(Product product) {
        return new ProductDocument(
                product.getId(),
                product.getBranchId(),
                product.getName(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    public static Product toDomain(ProductDocument document) {
        return Product.restore(
                document.id(),
                document.branchId(),
                document.name(),
                document.stock(),
                document.createdAt(),
                document.updatedAt());
    }
}

package com.kevinmartinez.franchise.infrastructure.persistence.mongo.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("products")
@CompoundIndex(name = "branch_stock_idx", def = "{'branchId': 1, 'stock': -1}")
public record ProductDocument(
        @Id String id,
        String branchId,
        String name,
        int stock,
        Instant createdAt,
        Instant updatedAt) {
}

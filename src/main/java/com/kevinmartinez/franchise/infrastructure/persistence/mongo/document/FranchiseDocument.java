package com.kevinmartinez.franchise.infrastructure.persistence.mongo.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("franchises")
public record FranchiseDocument(
        @Id String id,
        String name,
        Instant createdAt,
        Instant updatedAt) {
}

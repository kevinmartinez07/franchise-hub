package com.kevinmartinez.franchise.infrastructure.persistence.mongo.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("branches")
public record BranchDocument(
        @Id String id,
        @Indexed String franchiseId,
        String name,
        Instant createdAt,
        Instant updatedAt) {
}

package com.kevinmartinez.franchise.infrastructure.persistence.mongo.mapper;

import com.kevinmartinez.franchise.domain.model.Franchise;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.document.FranchiseDocument;

public final class FranchiseMongoMapper {

    private FranchiseMongoMapper() {
    }

    public static FranchiseDocument toDocument(Franchise franchise) {
        return new FranchiseDocument(
                franchise.getId(),
                franchise.getName(),
                franchise.getCreatedAt(),
                franchise.getUpdatedAt());
    }

    public static Franchise toDomain(FranchiseDocument document) {
        return Franchise.restore(
                document.id(),
                document.name(),
                document.createdAt(),
                document.updatedAt());
    }
}

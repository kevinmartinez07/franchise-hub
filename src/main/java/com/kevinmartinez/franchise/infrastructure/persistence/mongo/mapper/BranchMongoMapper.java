package com.kevinmartinez.franchise.infrastructure.persistence.mongo.mapper;

import com.kevinmartinez.franchise.domain.model.Branch;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.document.BranchDocument;

public final class BranchMongoMapper {

    private BranchMongoMapper() {
    }

    public static BranchDocument toDocument(Branch branch) {
        return new BranchDocument(
                branch.getId(),
                branch.getFranchiseId(),
                branch.getName(),
                branch.getCreatedAt(),
                branch.getUpdatedAt());
    }

    public static Branch toDomain(BranchDocument document) {
        return Branch.restore(
                document.id(),
                document.franchiseId(),
                document.name(),
                document.createdAt(),
                document.updatedAt());
    }
}

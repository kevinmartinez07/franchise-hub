package com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository;

import com.kevinmartinez.franchise.infrastructure.persistence.mongo.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ReactiveFranchiseMongoRepository
        extends ReactiveMongoRepository<FranchiseDocument, String> {
}

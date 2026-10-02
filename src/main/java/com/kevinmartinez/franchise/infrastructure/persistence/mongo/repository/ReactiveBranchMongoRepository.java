package com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository;

import com.kevinmartinez.franchise.infrastructure.persistence.mongo.document.BranchDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReactiveBranchMongoRepository
        extends ReactiveMongoRepository<BranchDocument, String> {

    Flux<BranchDocument> findByFranchiseId(String franchiseId);
}

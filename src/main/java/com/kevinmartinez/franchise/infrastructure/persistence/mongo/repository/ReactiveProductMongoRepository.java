package com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository;

import com.kevinmartinez.franchise.infrastructure.persistence.mongo.document.ProductDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveProductMongoRepository
        extends ReactiveMongoRepository<ProductDocument, String> {

    Flux<ProductDocument> findByBranchId(String branchId);

    Mono<ProductDocument> findFirstByBranchIdOrderByStockDesc(String branchId);
}

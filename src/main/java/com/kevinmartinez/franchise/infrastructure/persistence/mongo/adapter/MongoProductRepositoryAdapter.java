package com.kevinmartinez.franchise.infrastructure.persistence.mongo.adapter;

import java.util.Objects;

import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.domain.model.Product;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.mapper.ProductMongoMapper;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository.ReactiveProductMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MongoProductRepositoryAdapter implements ProductRepository {

    private final ReactiveProductMongoRepository repository;

    public MongoProductRepositoryAdapter(ReactiveProductMongoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public Mono<Product> save(Product product) {
        return repository.save(ProductMongoMapper.toDocument(product)).map(ProductMongoMapper::toDomain);
    }

    @Override
    public Flux<Product> findAll() {
        return repository.findAll().map(ProductMongoMapper::toDomain);
    }

    @Override
    public Mono<Product> findById(String id) {
        return repository.findById(id).map(ProductMongoMapper::toDomain);
    }

    @Override
    public Flux<Product> findByBranchId(String branchId) {
        return repository.findByBranchId(branchId).map(ProductMongoMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Product> findMaxStockByBranchId(String branchId) {
        return repository.findFirstByBranchIdOrderByStockDesc(branchId)
                .map(ProductMongoMapper::toDomain);
    }
}

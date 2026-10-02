package com.kevinmartinez.franchise.infrastructure.persistence.mongo.adapter;

import java.util.Objects;

import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.domain.model.Franchise;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.mapper.FranchiseMongoMapper;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository.ReactiveFranchiseMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MongoFranchiseRepositoryAdapter implements FranchiseRepository {

    private final ReactiveFranchiseMongoRepository repository;

    public MongoFranchiseRepositoryAdapter(ReactiveFranchiseMongoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchiseMongoMapper.toDocument(franchise))
                .map(FranchiseMongoMapper::toDomain);
    }

    @Override
    public Flux<Franchise> findAll() {
        return repository.findAll().map(FranchiseMongoMapper::toDomain);
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id).map(FranchiseMongoMapper::toDomain);
    }
}

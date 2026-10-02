package com.kevinmartinez.franchise.infrastructure.persistence.mongo.adapter;

import java.util.Objects;

import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.domain.model.Branch;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.mapper.BranchMongoMapper;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository.ReactiveBranchMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MongoBranchRepositoryAdapter implements BranchRepository {

    private final ReactiveBranchMongoRepository repository;

    public MongoBranchRepositoryAdapter(ReactiveBranchMongoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public Mono<Branch> save(Branch branch) {
        return repository.save(BranchMongoMapper.toDocument(branch)).map(BranchMongoMapper::toDomain);
    }

    @Override
    public Flux<Branch> findAll() {
        return repository.findAll().map(BranchMongoMapper::toDomain);
    }

    @Override
    public Mono<Branch> findById(String id) {
        return repository.findById(id).map(BranchMongoMapper::toDomain);
    }

    @Override
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        return repository.findByFranchiseId(franchiseId).map(BranchMongoMapper::toDomain);
    }
}

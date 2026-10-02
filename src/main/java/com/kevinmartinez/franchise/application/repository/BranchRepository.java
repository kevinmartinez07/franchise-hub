package com.kevinmartinez.franchise.application.repository;

import com.kevinmartinez.franchise.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> save(Branch branch);

    Flux<Branch> findAll();

    Mono<Branch> findById(String id);

    Flux<Branch> findByFranchiseId(String franchiseId);
}
